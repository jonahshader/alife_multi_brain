uniform mat4 u_projTrans;

uniform mat4 p_rotation;
uniform vec3 p_origin;

attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute vec4 a_color;

varying vec4 v_color;
varying vec2 v_texCoord;

void main() {
    gl_Position = u_projTrans * ((p_rotation * (a_position - vec4(p_origin.xyz, 0f))) + vec4(p_origin.xyz, 0f));
    v_texCoord = a_texCoord0;
    v_color = a_color;
}