package br.edu.dombosco.ptdb.video;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LinkVideo {

    private static final Pattern YOUTUBE = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?(?:[^#]*&)?v=|embed/|shorts/|live/)|youtu\\.be/)([A-Za-z0-9_-]{11})");

    private LinkVideo() {
    }

    public static boolean valido(String link) {
        if (link == null || link.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(link.trim());
            String esquema = uri.getScheme();
            return ("http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema))
                    && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static Optional<String> youtubeId(String link) {
        if (link == null) {
            return Optional.empty();
        }
        Matcher matcher = YOUTUBE.matcher(link);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    public static String capaYoutube(String link) {
        return youtubeId(link)
                .map(id -> "https://img.youtube.com/vi/" + id + "/hqdefault.jpg")
                .orElse(null);
    }
}
