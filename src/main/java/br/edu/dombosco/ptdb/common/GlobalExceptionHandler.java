package br.edu.dombosco.ptdb.common;

import br.edu.dombosco.ptdb.storage.StorageException;
import br.edu.dombosco.ptdb.web.Redirecionamento;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({RegraNegocioException.class, StorageException.class})
    public String tratarErroDeNegocio(RuntimeException ex, HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", ex.getMessage());
        return "redirect:" + Redirecionamento.paginaAnterior(request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tratarUploadGrande(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", "O arquivo enviado excede o tamanho máximo permitido.");
        return "redirect:" + Redirecionamento.paginaAnterior(request);
    }
}
