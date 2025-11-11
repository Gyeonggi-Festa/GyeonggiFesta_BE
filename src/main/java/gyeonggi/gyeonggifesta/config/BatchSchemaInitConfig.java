package gyeonggi.gyeonggifesta.config;//package gyeonggi.gyeonggifesta.config;
//
//import javax.sql.DataSource;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.DefaultResourceLoader;
//import org.springframework.core.io.Resource;
//import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
//import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
//
//@Configuration
//public class BatchSchemaInitConfig {
//
//    @Bean
//    public Object initBatchSchema(DataSource dataSource) {
//        // Spring Batch가 제공하는 MySQL용 스키마
//        Resource schema = new DefaultResourceLoader()
//                .getResource("classpath:org/springframework/batch/core/schema-mysql.sql");
//
//        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(schema);
//        // 이미 있으면 에러 안 나게
//        populator.setIgnoreFailedDrops(true);
//
//        DatabasePopulatorUtils.execute(populator, dataSource);
//        return new Object();
//    }
//}
