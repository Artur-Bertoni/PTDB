package br.edu.dombosco.ptdb.video;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkVideo {

    private static final Pattern YOUTUBE = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?(?:[^#]*&)?v=|embed/|shorts/|live/)|youtu\\.be/)([A-Za-z0-9_-]{11})");
    private static final Pattern VIMEO = Pattern.compile("vimeo\\.com/(?:video/)?(\\d+)");
    private static final Pattern GOOGLE_DRIVE = Pattern.compile("drive\\.google\\.com/file/d/([A-Za-z0-9_-]+)");

    public static boolean valido(String link) {
        if (link == null || link.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(link.trim());
            String esquema = uri.getScheme();
            boolean http = "http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema);
            return http && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static String youtubeId(String link) {
        return buscar(YOUTUBE, link);
    }

    public static String capaYoutube(String link) {
        String id = youtubeId(link);
        if (id == null) {
            return null;
        }
        return "https://img.youtube.com/vi/" + id + "/hqdefault.jpg";
    }

    public static String embed(String link) {
        String youtube = buscar(YOUTUBE, link);
        if (youtube != null) {
            return "https://www.youtube-nocookie.com/embed/" + youtube;
        }
        String vimeo = buscar(VIMEO, link);
        if (vimeo != null) {
            return "https://player.vimeo.com/video/" + vimeo;
        }
        String drive = buscar(GOOGLE_DRIVE, link);
        if (drive != null) {
            return "https://drive.google.com/file/d/" + drive + "/preview";
        }
        return null;
    }

    private static String buscar(Pattern padrao, String link) {
        if (link == null) {
            return null;
        }
        Matcher matcher = padrao.matcher(link);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
