package br.edu.dombosco.ptdb.common;

import br.edu.dombosco.ptdb.storage.StorageException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String REDIRECT_LISTA = "redirect:/admin/videos";

    @ExceptionHandler({RegraNegocioException.class, StorageException.class, RecursoNaoEncontradoException.class})
    public String tratarErroDeNegocio(RuntimeException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", ex.getMessage());
        return REDIRECT_LISTA;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tratarUploadGrande(MaxUploadSizeExceededException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", "O arquivo enviado excede o tamanho máximo permitido.");
        return REDIRECT_LISTA;
    }
}
