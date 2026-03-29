#version 330 core
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec2 aTexCoord;
layout(location = 3) in float aSkyLight;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 Normal;
out vec2 TexCoord;
out float SkyLight;
out float FogDistance;

void main() {
    vec4 worldPos = model * vec4(aPos, 1.0);
    vec4 viewPos = view * worldPos;

    Normal = aNormal;
    TexCoord = aTexCoord;
    SkyLight = aSkyLight;
    FogDistance = length(viewPos.xyz);

    gl_Position = projection * viewPos;
}