package pl.north93.zgame.api.global.component.impl;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javassist.ClassPool;
import javassist.LoaderClassPath;
import pl.north93.zgame.api.global.ApiCore;
import pl.north93.zgame.api.global.component.ComponentDescription;
import pl.north93.zgame.api.global.component.annotations.SkipInjections;

class ClassloaderScanningTask
{
    private final ComponentManagerImpl        manager;
    private final ClassLoader                 classLoader;
    private final URL                         loadedFile;
    private final ClassPool                   classPool;
    private final Queue<AbstractScanningTask> pendingTasks;

    public ClassloaderScanningTask(final ComponentManagerImpl manager, final ClassLoader classLoader, final URL loadedFile)
    {
        this.manager = manager;
        this.classLoader = classLoader;
        this.loadedFile = loadedFile;
        this.classPool = this.createClassPool();
        this.pendingTasks = new ArrayDeque<>();
    }

    public static ClassloaderScanningTask create(final ComponentManagerImpl manager, final ClassLoader classLoader)
    {
        if (classLoader instanceof JarComponentLoader)
        {
            return new ClassloaderScanningTask(manager, classLoader, ((JarComponentLoader) classLoader).getFileUrl());
        }
        else if (classLoader == ClassloaderScanningTask.class.getClassLoader())
        {
            return new ClassloaderScanningTask(manager, classLoader, ClassloaderScanningTask.class.getProtectionDomain().getCodeSource().getLocation());
        }
        throw new IllegalArgumentException();
    }

    public void scanWithoutComponents(final String rootPackage, final List<ComponentDescription> components)
    {
        final FilterBuilder filter = new FilterBuilder();
        filter.includePackage(rootPackage);
        for (final ComponentDescription component : components)
        {
            final String pack;
            if (StringUtils.isEmpty(component.getPackageToScan()))
            {
                final int lastDot = component.getMainClass().lastIndexOf('.');
                pack = component.getMainClass().substring(0, lastDot);
            }
            else
            {
                pack = component.getPackageToScan();
            }
            filter.excludePackage(pack);
        }
        this.scan(filter);
    }

    public void scanComponent(final ComponentBundle bundle)
    {
        final FilterBuilder filter = new FilterBuilder();
        for (final String pack : bundle.getBasePackages())
        {
            filter.includePackage(pack);
        }
        this.scan(filter);
    }

    /*default*/ void scan(final FilterBuilder filter)
    {
        final Reflections reflections = this.createReflections(filter);

        final Set<Class<?>> allClasses = this.getAllClasses(reflections);
        for (final Class<?> aClass : allClasses)
        {
            if (aClass.isAnnotationPresent(SkipInjections.class))
            {
                continue;
            }
            final AbstractBeanContext beanContext = this.manager.getOwningContext(aClass);
            this.pendingTasks.add(new StaticScanningTask(this, aClass, beanContext));
            this.pendingTasks.add(new InjectorInstallScanningTask(this, aClass, beanContext));
            this.pendingTasks.add(new MethodScanningTask(this, aClass, beanContext));
        }

        final Iterator<AbstractScanningTask> iterator = this.pendingTasks.iterator();
        boolean modified = true;
        while (iterator.hasNext() && modified)
        {
            modified = false;
            final AbstractScanningTask task = iterator.next();
            if (task.tryComplete())
            {
                modified = true;
                iterator.remove();
            }
        }

        if (! this.pendingTasks.isEmpty())
        {
            throw new RuntimeException("There're uncompletable class processing tasks."); // todo other exception
        }

        for (final Class<?> aClass : allClasses)
        {
            final AbstractBeanContext beanContext = this.manager.getOwningContext(aClass);
            this.manager.getAggregationManager().call(beanContext, aClass);
        }
    }

    /*default*/ Set<Class<?>> getAllClasses(final Reflections reflections)
    {
        final Collection<String> classes = reflections.getStore().get(SubTypesScanner.class.getSimpleName()).values();
        final Set<Class<?>> out = new HashSet<>(classes.size());
        for (final String clazz : classes)
        {
            try
            {
                final Class<?> outClass = forName(clazz, reflections.getConfiguration().getClassLoaders());
                if (outClass != null)
                {
                    outClass.getDeclaredFields();
                    out.add(outClass);
                }
            }
            catch (final Throwable e)
            {
                // ignore class
            }
        }
        return out;
    }

    private static Class<?> forName(final String type, ClassLoader... classLoaders)
    {
        classLoaders = ClasspathHelper.classLoaders(classLoaders);
        int var5 = classLoaders.length;
        int var6 = 0;

        while (var6 < var5)
        {
            final ClassLoader classLoader = classLoaders[var6];
            try
            {
                return classLoader.loadClass(type);
            }
            catch (final Throwable var9)
            {
                ++ var6;
            }
        }

        return null;
    }

    /*default*/ ClassPool getClassPool()
    {
        return this.classPool;
    }

    private ClassPool createClassPool()
    {
        if (this.classLoader instanceof JarComponentLoader)
        {
            final JarComponentLoader componentLoader = (JarComponentLoader) this.classLoader;
            return componentLoader.getClassPool();
        }
        else
        {
            final ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(ApiCore.class.getClassLoader()));
            classPool.appendClassPath(new LoaderClassPath(this.classLoader));
            return classPool;
        }
    }

    private Reflections createReflections(final FilterBuilder packageFilter)
    {
        final ConfigurationBuilder configuration = new ConfigurationBuilder();
        configuration.setClassLoaders(new ClassLoader[]{this.classLoader});
        configuration.setScanners(new SubTypesScanner(false), new MethodAnnotationsScanner(), new FieldAnnotationsScanner());
        configuration.setUrls(this.loadedFile);
        configuration.filterInputsBy(packageFilter);

        return new Reflections(configuration);
    }
}
