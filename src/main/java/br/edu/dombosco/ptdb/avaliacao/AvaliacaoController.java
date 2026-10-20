package br.edu.dombosco.ptdb.avaliacao;

import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import br.edu.dombosco.ptdb.video.Video;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/avaliacao")
public class AvaliacaoController {

    private static final String VIEW_FILA = "avaliacao/fila";

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    public String fila(@RequestParam(name = "video", required = false) Long videoId, Model model) {
        prepararFila(model, videoId, new AvaliacaoForm());
        return VIEW_FILA;
    }

    @PostMapping("/{videoId}")
    public String avaliar(@PathVariable Long videoId,
                          @ModelAttribute("form") AvaliacaoForm form,
                          BindingResult result,
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirect) {
        avaliacaoService.validar(form, result);
        if (result.hasErrors()) {
            prepararFila(model, videoId, form);
            return VIEW_FILA;
        }
        UsuarioLogado avaliador = UsuarioSessao.buscar(session);
        Avaliacao avaliacao = avaliacaoService.avaliar(videoId, avaliador.getId(), form.getDecisao(), form.getParecer());
        redirect.addFlashAttribute("mensagem",
                "\"" + avaliacao.getVideo().getTitulo() + "\": " + form.getDecisao().getMensagem());
        return "redirect:/avaliacao";
    }

    @GetMapping("/historico")
    public String historico(Model model) {
        model.addAttribute("avaliacoes", avaliacaoService.historico());
        model.addAttribute("pendentes", avaliacaoService.fila().size());
        return "avaliacao/historico";
    }

    private void prepararFila(Model model, Long videoId, AvaliacaoForm form) {
        List<Video> fila = avaliacaoService.fila();

        Video selecionado = null;
        for (Video video : fila) {
            if (video.getId().equals(videoId)) {
                selecionado = video;
            }
        }
        if (selecionado == null && !fila.isEmpty()) {
            selecionado = fila.get(0);
        }

        List<Avaliacao> historicoVideo = new ArrayList<>();
        if (selecionado != null) {
            historicoVideo = avaliacaoService.historicoDoVideo(selecionado.getId());
        }

        model.addAttribute("fila", fila);
        model.addAttribute("pendentes", fila.size());
        model.addAttribute("selecionado", selecionado);
        model.addAttribute("historicoVideo", historicoVideo);
        model.addAttribute("form", form);
    }
}
