package br.edu.dombosco.ptdb.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

public class Redirecionamento {

    public static String paginaAnterior(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            return "/";
        }
        try {
            URI uri = new URI(referer);
            String caminho = uri.getRawPath();
            if (caminho == null || !caminho.startsWith("/") || caminho.startsWith("//")) {
                return "/";
            }
            if (uri.getRawQuery() != null) {
                caminho = caminho + "?" + uri.getRawQuery();
            }
            return caminho;
        } catch (URISyntaxException e) {
            return "/";
        }
    }
}
