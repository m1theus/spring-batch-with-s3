package dev.mmartins.sb;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.ItemProcessor;


import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.PreparedStatement;
import java.util.List;


@Configuration
public class JobConfig {
    private final S3Service s3Service;
    private final JdbcTemplate jdbcTemplate;
    private static final int CHUNK_SIZE = 50;

    public JobConfig(final S3Service s3Service, final JdbcTemplate jdbcTemplate) {
        this.s3Service = s3Service;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public Job importPersonJob(final JobRepository jobRepository,
                               final Step processPersonDataStep,
                               final JobCompletionNotificationListener listener) {
        return new JobBuilder("importPersonJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(processPersonDataStep)
                .build();
    }

    @Bean
    public Step processPersonDataStep(final JobRepository jobRepository,
                                      final PlatformTransactionManager transactionManager,
                                      final ItemProcessor<String, Person> personItemProcessor,
                                      final ItemWriter<Person> personDatabaseWriter,
                                      final FlatFileItemReader<String> s3ItemReader) {
        return new StepBuilder("processPersonDataStep", jobRepository)
                .<String, Person>chunk(CHUNK_SIZE, transactionManager)
                .reader(s3ItemReader)
                .processor(personItemProcessor)
                .writer(personDatabaseWriter)
                .build();
    }

    @Bean
    public FlatFileItemReader<String> s3ItemReader() {
        return new FlatFileItemReaderBuilder<String>()
                .name("s3ItemReader")
                .resource(new InputStreamResource(s3Service.getObjectFile("PEOPLE.txt")))
                .lineTokenizer(new DelimitedLineTokenizer("\n"))
                .fieldSetMapper(fieldSet -> fieldSet.readRawString(0))
                .build();
    }

    @Bean
    public ItemProcessor<String, Person> personItemProcessor() {
        return item -> {
            if (item.trim().isEmpty()) return null;
            final var firstName = item.substring(0, 50).trim();
            final var lastName = item.substring(50, 100).trim();
            final var age = Integer.parseInt(item.substring(100, 150).trim());
            final var role = item.substring(150, 200).trim();
            return new Person(firstName, lastName, age, role);
        };
    }

    @Bean
    public ItemWriter<Person> personDatabaseWriter() {
        return items -> this.batchUpdate((List<Person>) items.getItems());
    }

    public void batchUpdate(final List<Person> personList) {
        jdbcTemplate.batchUpdate("INSERT INTO people (first_name, last_name, age, role) " +
                        "VALUES (?, ?, ?, ?)",
                personList,
                CHUNK_SIZE,
                (final PreparedStatement ps, final Person person) -> {
                    ps.setString(1, person.firstName());
                    ps.setString(2, person.lastName());
                    ps.setInt(3, person.age());
                    ps.setString(4, person.role());
                });
    }

}
