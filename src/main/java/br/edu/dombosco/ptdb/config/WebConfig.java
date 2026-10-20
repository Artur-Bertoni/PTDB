package br.edu.dombosco.ptdb.config;

import br.edu.dombosco.ptdb.security.PerfilInterceptor;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.security.UsuarioPadraoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UsuarioPadraoInterceptor usuarioPadraoInterceptor;

    public WebConfig(UsuarioPadraoInterceptor usuarioPadraoInterceptor) {
        this.usuarioPadraoInterceptor = usuarioPadraoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(usuarioPadraoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/favicon.ico", "/error", "/h2-console/**");

        registry.addInterceptor(new PerfilInterceptor(PerfilUsuario.ADMINISTRADOR))
                .addPathPatterns("/admin/**");
        registry.addInterceptor(new PerfilInterceptor(PerfilUsuario.AVALIADOR))
                .addPathPatterns("/avaliacao/**");
        registry.addInterceptor(new PerfilInterceptor(PerfilUsuario.ALUNO))
                .addPathPatterns("/favoritos/**");
    }
}
