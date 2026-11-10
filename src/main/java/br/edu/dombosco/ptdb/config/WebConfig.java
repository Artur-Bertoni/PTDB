package br.edu.dombosco.ptdb.config;

import br.edu.dombosco.ptdb.security.AutenticacaoInterceptor;
import br.edu.dombosco.ptdb.security.PerfilInterceptor;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AutenticacaoInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/perfil", "/tela_login.html", "/css/**", "/js/**", "/img/**",
                        "/favicon.ico", "/error", "/h2-console/**");

        registry.addInterceptor(new PerfilInterceptor(PerfilUsuario.ADMINISTRADOR))
                .addPathPatterns("/api/admin/**", "/tela_painel.html", "/tela_categorias.html",
                        "/tela_gerenciar_faq.html", "/tela_usuarios.html");
        registry.addInterceptor(new PerfilInterceptor(PerfilUsuario.AVALIADOR))
                .addPathPatterns("/api/avaliacao/**", "/tela_avaliacao.html");
        registry.addInterceptor(new PerfilInterceptor(PerfilUsuario.ALUNO))
                .addPathPatterns("/api/favoritos/**", "/tela_favoritos.html");
        registry.addInterceptor(new PerfilInterceptor(PerfilUsuario.ADMINISTRADOR, PerfilUsuario.ALUNO))
                .addPathPatterns("/api/gestao/**", "/tela_gerenciar_videos.html");
    }
}
