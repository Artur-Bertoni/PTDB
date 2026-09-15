package br.edu.dombosco.ptdb.common;

import br.edu.dombosco.ptdb.storage.StorageException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Tratamento centralizado de erros de negocio/armazenamento do RF06,
 * convertendo-os em mensagens de erro exibidas na listagem de videos.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({RegraNegocioException.class, StorageException.class})
    public String tratarRegraNegocio(RuntimeException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/admin/videos";
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public String tratarNaoEncontrado(RecursoNaoEncontradoException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/admin/videos";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tratarUploadGrande(MaxUploadSizeExceededException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro",
                "O arquivo enviado excede o tamanho maximo permitido.");
        return "redirect:/admin/videos";
    }
}
