    package gyeonggi.gyeonggifesta.config;

    import com.github.benmanes.caffeine.cache.Caffeine;
    import org.springframework.cache.CacheManager;
    import org.springframework.cache.caffeine.CaffeineCacheManager;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
    import org.springframework.cache.annotation.EnableCaching;

    import java.time.Duration;
    import java.util.concurrent.Executor;

    @Configuration
    @EnableCaching
    public class CacheConfig {

        @Bean
        public CacheManager cacheManager() {
            CaffeineCacheManager m = new CaffeineCacheManager("parkingRows");
            m.setCaffeine(Caffeine.newBuilder()
                    .maximumSize(2000)                      // 시군별 rows 캐시 상한
                    .expireAfterWrite(Duration.ofMinutes(10)) // 10분 TTL (원하면 5분)
            );
            return m;
        }

        @Bean(name = "parkingExecutor")
        public Executor parkingExecutor() {
            ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
            ex.setThreadNamePrefix("parking-");
            ex.setCorePoolSize(8);   // CPU/트래픽 보고 조정
            ex.setMaxPoolSize(16);
            ex.setQueueCapacity(200);
            ex.initialize();
            return ex;
        }
    }
