#version 120

uniform sampler2D uTex;
uniform vec2 uResolution;
uniform float uRadius;
uniform float uPreserveAlpha;

varying vec4 texCoord;

vec4 sampleClamped(sampler2D tex, vec2 uv) {
    vec2 clampedUV = clamp(uv, 0.001, 0.999);
    return texture2D(tex, clampedUV);
}

void main() {
    vec2 uv = texCoord.xy;
    vec2 texelSize = uRadius / uResolution;

    vec4 color = sampleClamped(uTex, uv) * 4.0;

    color += sampleClamped(uTex, uv - texelSize);
    color += sampleClamped(uTex, uv + texelSize);
    color += sampleClamped(uTex, uv + vec2(texelSize.x, -texelSize.y));
    color += sampleClamped(uTex, uv - vec2(texelSize.x, -texelSize.y));

    color += sampleClamped(uTex, uv + vec2(-texelSize.x, 0.0)) * 2.0;
    color += sampleClamped(uTex, uv + vec2( texelSize.x, 0.0)) * 2.0;
    color += sampleClamped(uTex, uv + vec2(0.0, -texelSize.y)) * 2.0;
    color += sampleClamped(uTex, uv + vec2(0.0,  texelSize.y)) * 2.0;

    color = color / 16.0;
    color.a = mix(1.0, color.a, uPreserveAlpha);

    gl_FragColor = color;
}