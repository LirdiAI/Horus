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
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise2(p);
        p = p * 2.0 + vec2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

vec3 nebulaShaderColor(vec3 worldDirection) {
    vec3 dir = normalize(worldDirection);
    float h = dir.y;

    // Deep space base
    vec3 col = mix(vec3(0.010, 0.012, 0.030), vec3(0.020, 0.030, 0.075), smoothstep(0.0, 0.8, h));

    // Project direction onto a plane for the nebula
    vec2 uv = dir.xz / max(0.35 + dir.y * 0.85, 0.12);
    uv *= 0.9;

    float t = time * 0.05;
    float n1 = fbm(uv * 1.6 + vec2(t, -t * 0.6));
    float n2 = fbm(uv * 3.1 - vec2(t * 0.7, t * 0.4) + n1);
    float nebula = smoothstep(0.42, 0.85, n1 * 0.65 + n2 * 0.55);

    vec3 c1 = vec3(0.55, 0.16, 0.85);  // purple
    vec3 c2 = vec3(0.16, 0.55, 0.95);  // blue
    vec3 c3 = vec3(0.95, 0.30, 0.55);  // pink

    vec3 nebCol = mix(c2, c1, n2);
    nebCol = mix(nebCol, c3, smoothstep(0.55, 0.95, n1) * 0.6);

    col += nebCol * nebula * 0.85;

    // Stars
    vec2 sp = dir.xz / max(0.12 + dir.y, 0.08) * 26.0;
    vec2 cell = floor(sp);
    vec2 f = fract(sp) - 0.5;
    float star = step(0.982, hash21(cell));
    float twinkle = 0.55 + 0.45 * sin(time * 2.6 + hash21(cell + 3.7) * 6.28);
    float starMask = smoothstep(0.28, 0.0, length(f)) * star * smoothstep(0.1, 0.5, h);
    col += vec3(1.0) * starMask * twinkle * 0.9;

    return col;
}

void main() {
    vec3 skyDirection = normalize(vRay);
    vec3 shaderColor = nebulaShaderColor(skyDirection);
    float amount = clamp(max(Intensity, 0.0) * FirstColor.a * 0.84, 0.0, 1.0);

    OutColor = vec4(shaderColor, fullSkyAmount(skyDirection.y, amount));
}
