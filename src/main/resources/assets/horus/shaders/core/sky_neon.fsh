#version 150

layout(std140) uniform SkyPostParams {
    mat4 InvViewProj;
    vec4 FirstColor;
    vec4 SecondColor;
    vec4 TimeIntensity;
    vec4 CameraPosition;
};

#define time (TimeIntensity.x)
#define Intensity (TimeIntensity.y)

in vec3 vRay;
in vec2 TexCoord;

out vec4 OutColor;

float fullSkyAmount(float skyHeight, float amount) {
    float lowerSky = 1.0 - smoothstep(-0.18, 0.18, skyHeight);
    float enabled = smoothstep(0.02, 0.18, amount);
    return clamp(mix(amount, max(amount, 0.96), lowerSky * enabled), 0.0, 1.0);
}

float hash21(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

vec3 neonShaderColor(vec3 worldDirection) {
    vec3 dir = normalize(worldDirection);
    float h = dir.y;

    // Gradient: deep space top -> magenta -> hot horizon
    vec3 top = vec3(0.035, 0.015, 0.110);
    vec3 mid = vec3(0.310, 0.070, 0.420);
    vec3 horizon = vec3(0.980, 0.260, 0.560);
    vec3 col = mix(horizon, mid, smoothstep(0.0, 0.32, h));
    col = mix(col, top, smoothstep(0.28, 0.78, h));

    // Sun direction: fixed, low above the horizon
    vec3 sunDir = normalize(vec3(0.0, 0.10, -1.0));
    float ang = acos(clamp(dot(dir, sunDir), -1.0, 1.0));
    float disc = 1.0 - smoothstep(0.150 - 0.012, 0.150 + 0.012, ang);
    float glow = exp(-ang * 6.0);

    // Retro scanline bands cutting the sun
    float bands = smoothstep(0.0, 0.30, h);
    float bandPhase = dir.y * 70.0 - time * 2.0;
    float band = step(0.45, fract(bandPhase));
    float cut = mix(1.0, band, disc * (1.0 - bands));

    vec3 sunColor = vec3(1.0, 0.86, 0.26);
    vec3 glowColor = vec3(1.0, 0.32, 0.58);

    // Stars at the top
    vec2 sp = dir.xz / max(0.15 + dir.y, 0.1) * 22.0;
    vec2 cell = floor(sp);
    vec2 f = fract(sp) - 0.5;
    float star = step(0.985, hash21(cell));
    float twinkle = 0.6 + 0.4 * sin(time * 3.0 + hash21(cell + 7.0) * 6.28);
    float starMask = smoothstep(0.25, 0.0, length(f)) * star * smoothstep(0.18, 0.55, h);

    // Compose
    col += glowColor * glow * 0.55;
    col = mix(col, sunColor, disc * cut);
    col += vec3(1.0, 0.9, 0.8) * starMask * 0.9;

    return col;
}

void main() {
    vec3 skyDirection = normalize(vRay);
    vec3 shaderColor = neonShaderColor(skyDirection);
    float amount = clamp(max(Intensity, 0.0) * FirstColor.a * 0.84, 0.0, 1.0);

    OutColor = vec4(shaderColor, fullSkyAmount(skyDirection.y, amount));
}
