package br.edu.dombosco.ptdb.video;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LinkVideoTest {

    @Test
    void aceitaSomenteLinksHttpEHttps() {
        assertThat(LinkVideo.valido("https://www.youtube.com/watch?v=dQw4w9WgXcQ")).isTrue();
        assertThat(LinkVideo.valido("http://exemplo.com/video.mp4")).isTrue();
        assertThat(LinkVideo.valido("javascript:alert(1)")).isFalse();
        assertThat(LinkVideo.valido("ftp://exemplo.com/video.mp4")).isFalse();
        assertThat(LinkVideo.valido("exemplo.com/video")).isFalse();
        assertThat(LinkVideo.valido("  ")).isFalse();
    }

    @Test
    void extraiCapaDosFormatosDeLinkDoYoutube() {
        String capa = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg";
        assertThat(LinkVideo.capaYoutube("https://www.youtube.com/watch?v=dQw4w9WgXcQ")).isEqualTo(capa);
        assertThat(LinkVideo.capaYoutube("https://www.youtube.com/watch?list=abc&v=dQw4w9WgXcQ")).isEqualTo(capa);
        assertThat(LinkVideo.capaYoutube("https://youtu.be/dQw4w9WgXcQ?t=10")).isEqualTo(capa);
        assertThat(LinkVideo.capaYoutube("https://www.youtube.com/shorts/dQw4w9WgXcQ")).isEqualTo(capa);
        assertThat(LinkVideo.capaYoutube("https://vimeo.com/123456")).isNull();
    }
}
