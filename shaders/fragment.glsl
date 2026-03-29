#version 330 core
in vec3 Normal;
in vec2 TexCoord;
in float SkyLight;
in float FogDistance;

out vec4 FragColor;

uniform sampler2D texSampler;
uniform float ambientStrength;
uniform vec3 lightColor;
uniform vec3 tint;
uniform vec3 fogColor;
uniform float fogStart;
uniform float fogEnd;

void main() {
    vec3 norm = normalize(Normal);

    float faceBrightness;
    if (norm.y > 0.9) {
        faceBrightness = 1.0;
    } else if (norm.y < -0.9) {
        faceBrightness = 0.4;
    } else {
        faceBrightness = 0.7;
    }

    float light = 0.2 + SkyLight * 0.8;

    vec4 texColor = texture(texSampler, TexCoord);
    vec3 litColor = ambientStrength * faceBrightness * light * lightColor * texColor.rgb * tint;

    float fogFactor = clamp((fogEnd - FogDistance) / (fogEnd - fogStart), 0.0, 1.0);
    vec3 finalColor = mix(fogColor, litColor, fogFactor);

    FragColor = vec4(finalColor, 1.0);
}