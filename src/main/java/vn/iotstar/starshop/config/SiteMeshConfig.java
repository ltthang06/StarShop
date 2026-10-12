package vn.iotstar.starshop.config;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.sitemesh.builder.SiteMeshFilterBuilder;
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="starshop.sitemesh.enabled",havingValue="true",matchIfMissing=true)
public class SiteMeshConfig {
    @Bean public FilterRegistrationBean<ConfigurableSiteMeshFilter> managerDecorator() {
        var registration=new FilterRegistrationBean<ConfigurableSiteMeshFilter>();
        registration.setFilter(new ConfigurableSiteMeshFilter() {
            @Override protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
                builder.addDecoratorPath("/manager", "manager.jsp")
                    .addDecoratorPath("/manager/*", "manager.jsp")
                    .addExcludedPath("/manager/reports/export");
            }
        });
        registration.addUrlPatterns("/manager", "/manager/*");
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setOrder(10);
        return registration;
    }
}
