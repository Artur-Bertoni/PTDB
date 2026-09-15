package br.edu.dombosco.ptdb.config;

import br.edu.dombosco.ptdb.security.AdministradorInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracao web MVC.
 *
 * <p>Registra o {@link AdministradorInterceptor} para proteger as rotas
 * administrativas ({@code /admin/**}), conforme a regra do RF06 de restringir
 * o gerenciamento de videos ao Administrador.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdministradorInterceptor administradorInterceptor;

    public WebConfig(AdministradorInterceptor administradorInterceptor) {
        this.administradorInterceptor = administradorInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(administradorInterceptor)
                .addPathPatterns("/admin/**");
    }
}
