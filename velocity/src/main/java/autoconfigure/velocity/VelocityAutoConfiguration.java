package autoconfigure.velocity;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

import javax.servlet.Servlet;

/**
 * Sets up Velocity template resolution through a Spring Boot autoconfiguration
 *
 * @author Josh Long
 */
@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class VelocityAutoConfiguration {
    public static final String DEFAULT_PREFIX = "/templates/";
    public static final String DEFAULT_SUFFIX = ".vm";

    @Configuration
    @ConditionalOnClass({Servlet.class})
    protected static class VelocityViewResolverConfiguration implements EnvironmentAware {

        private RelaxedPropertyResolver environment;

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = new RelaxedPropertyResolver(environment,
                    "spring.velocity.");
        }

        @Bean
        @ConditionalOnMissingBean
        VelocityConfigurer velocityConfig() {
            return new VelocityConfigurer();
        }

        @Bean
        @ConditionalOnMissingBean(name = "velocityViewResolver")
        VelocityViewResolver velocityViewResolver() {
            VelocityViewResolver resolver = new VelocityViewResolver();
            resolver.setSuffix(this.environment.getProperty("suffix", DEFAULT_SUFFIX)  );
            resolver.setPrefix(this.environment.getProperty("prefix", DEFAULT_PREFIX)  );
            // Needs to come before any fallback resolver (e.g. a
            // InternalResourceViewResolver)
            resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 20);
            return resolver;
        }
    }
}
