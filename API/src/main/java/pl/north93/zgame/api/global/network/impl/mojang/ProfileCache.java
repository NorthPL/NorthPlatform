package pl.north93.zgame.api.global.network.impl.mojang;

import static java.text.MessageFormat.format;

import static pl.north93.zgame.api.global.network.impl.mojang.MojangCacheImpl.JSON_PARSER;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import lombok.extern.slf4j.Slf4j;
import pl.north93.zgame.api.global.component.annotations.bean.Bean;
import pl.north93.zgame.api.global.network.mojang.CachedProfile;
import pl.north93.zgame.api.global.network.mojang.CachedProfileProperty;
import pl.north93.zgame.api.global.storage.StorageConnector;

@Slf4j
/*default*/ class ProfileCache
{
    private final Datastore datastore;

    @Bean
    private ProfileCache(final StorageConnector connector)
    {
        this.datastore = connector.getDatastore();
    }

    public void updateProfile(final CachedProfile profile)
    {
        this.datastore.save(profile);
    }

    public Optional<CachedProfile> getProfile(final UUID profileId)
    {
        final Query<CachedProfile> query = this.datastore.createQuery(CachedProfile.class)
                                                         .field("uuid").equal(profileId);

        final CachedProfile cachedProfile = query.get();
        if (cachedProfile == null)
        {
            return this.queryMojangAndFillCache(profileId);
        }

        return Optional.of(cachedProfile);
    }

    private Optional<CachedProfile> queryMojangAndFillCache(final UUID uuid)
    {
        final String url = this.composeProfileFetchUrl(uuid);

        try
        {
            final String response = IOUtils.toString(new URL(url));
            if (StringUtils.isEmpty(response))
            {
                return Optional.empty();
            }

            final JsonObject jsonObject = JSON_PARSER.parse(response).getAsJsonObject();

            final String name = jsonObject.get("name").getAsString();

            final JsonArray jsonProperties = jsonObject.get("properties").getAsJsonArray();
            final List<CachedProfileProperty> properties = this.buildPropertiesList(jsonProperties);

            final CachedProfile cachedProfile = new CachedProfile(uuid, name, properties);
            this.datastore.save(cachedProfile);

            log.info("Fetched profile from Mojang {}", cachedProfile);
            return Optional.of(cachedProfile);
        }
        catch (final Exception exception)
        {
            log.error("Failed to fetch {} profile", uuid, exception);
            return Optional.empty();
        }
    }

    private List<CachedProfileProperty> buildPropertiesList(final JsonArray jsonArray)
    {
        final List<CachedProfileProperty> properties = new ArrayList<>(jsonArray.size());
        for (final JsonElement element : jsonArray)
        {
            final JsonObject object = element.getAsJsonObject();

            final String name = object.get("name").getAsString();
            final String value = object.get("value").getAsString();
            final String signature = object.has("signature") ? object.get("signature").getAsString() : null;

            properties.add(new CachedProfileProperty(name, value, signature));
        }

        return properties;
    }

    private String composeProfileFetchUrl(final UUID profileId)
    {
        final String mojangUuid = StringUtils.remove(profileId.toString(), '-');
        return format("https://sessionserver.mojang.com/session/minecraft/profile/{0}?unsigned=false", mojangUuid);
    }
}
