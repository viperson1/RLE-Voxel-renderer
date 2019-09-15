#version 430

#pragma kernel main

uniform float PI = 3.141593;

layout(std430, binding = 2) buffer shader_data {
    vec3 eyePos;
    float direction;
    float horizon;

    vec2[][][] levelArr;
};

void main() {
    float rayDeg = mix(direction - (PI * .25f), direction + (PI * .25f), gl_GlobalInvocationID.x / (gl_NumWorkGroups.x * gl_WorkGroupSize.x));

    vec2 rayDir = { -sin(rayDeg), -cos(rayDeg) };
    rayDir.x = clamp(rayDir.x, 0, 2*PI);
    rayDir.y = clamp(rayDir.y, 0, 2*PI);

    return null;
}
