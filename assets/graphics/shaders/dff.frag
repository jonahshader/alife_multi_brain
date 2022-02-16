//#ifdef GL_ES
//precision mediump float;
//#endif
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;

uniform float p_spread;
uniform float p_renderScale;
uniform float p_distOffset;

void main() {
    float smoothing = 0.25/(p_spread * p_renderScale);
    float distance = texture2D(u_texture, v_texCoord).a + p_distOffset;
    float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
    gl_FragColor = vec4(v_color.rgb, v_color.a * alpha);
}