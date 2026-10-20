package org.greta.eshop_api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

// Ici plutôt que sur la classe principale : un @DataJpaTest charge toujours la classe principale,
// mais pas cette configuration, et refuserait de démarrer sans gestionnaire de cache
@Configuration
@EnableCaching
public class CacheConfig {
}
