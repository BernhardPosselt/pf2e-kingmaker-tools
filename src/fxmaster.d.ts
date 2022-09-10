interface WeatherOptions {
    scale?: number;
    tint?: {
        value: string;
        apply: boolean;
    };
    lifetime?: number;
    speed?: number;
}

interface DirectionOptions {
    direction?: number;
}

interface DensityOptions {
    density?: number;
}

interface AnimationOptions<T> {
    animations?: T[];
}

interface SnowstormWeatherEffect {
    type: 'snowstorm';
    options?: WeatherOptions & DensityOptions & DirectionOptions;
}

interface BubblesWeatherEffect {
    type: 'bubbles';
    options?: WeatherOptions & DensityOptions;
}

interface CloudsWeatherEffect {
    type: 'clouds';
    options?: WeatherOptions & DirectionOptions;
}

interface EmbersWeatherEffect {
    type: 'embers';
    options?: WeatherOptions & DensityOptions;
}

interface RainsimpleWeatherEffect {
    type: 'rainsimple';
    options?: WeatherOptions & DensityOptions & DirectionOptions;
}

interface StarsWeatherEffect {
    type: 'stars';
    options?: WeatherOptions & DensityOptions;
}

interface CrowsWeatherEffect {
    type: 'crows';
    options?: WeatherOptions & DensityOptions;
}

interface BatsWeatherEffect {
    type: 'bats';
    options?: WeatherOptions & DensityOptions;
}

interface SpidersWeatherEffect {
    type: 'spiders';
    options?: WeatherOptions & DensityOptions;
}

interface FogWeatherEffect {
    type: 'fog';
    options?: WeatherOptions & DensityOptions;
}

interface RaintopWeatherEffect {
    type: 'raintop';
    options?: WeatherOptions & DensityOptions & DirectionOptions;
}

interface BirdsWeatherEffect {
    type: 'birds';
    options?: WeatherOptions & DensityOptions & AnimationOptions<'glide' | 'flap' | 'mixed'>;
}

interface LeavesWeatherEffect {
    type: 'leaves';
    options?: WeatherOptions & DensityOptions;
}

interface RainWeatherEffect {
    type: 'rain';
    options?: WeatherOptions & DensityOptions & DirectionOptions;
}

interface SnowWeatherEffect {
    type: 'snow';
    options?: WeatherOptions & DensityOptions & DirectionOptions;
}

interface EaglesWeatherEffect {
    type: 'eagles';
    options?: WeatherOptions & DensityOptions & AnimationOptions<'glide' | 'flap'>;
}

export type WeatherEffects =
    SnowstormWeatherEffect
    | BubblesWeatherEffect
    | CloudsWeatherEffect
    | EmbersWeatherEffect
    | RainsimpleWeatherEffect
    | StarsWeatherEffect
    | CrowsWeatherEffect
    | BatsWeatherEffect
    | SpidersWeatherEffect
    | FogWeatherEffect
    | RaintopWeatherEffect
    | BirdsWeatherEffect
    | LeavesWeatherEffect
    | RainWeatherEffect
    | SnowWeatherEffect
    | EaglesWeatherEffect;

interface LightningEffect {
    type: 'lightning';
    options?: {
        frequency?: number;
        spark_duration?: number;
        brightness?: number;
    }
}

interface UnderwaterEffect {
    type: 'underwater';
    options?: {
        speed?: number;
        scale?: number;
    }
}

interface PredatorEffect {
    type: 'predator';
    options?: {
        noise?: number;
        period?: number;
        lineWidth?: number;
    }
}

interface ColorEffect {
    type: 'color';
    options?: {
        color?: {
            value: string;
            apply: boolean;
        };
        saturation?: number;
        contrast?: number;
        brightness?: number;
        gamma?: number;
    }
}

interface BloomEffect {
    type: 'bloom';
    options?: {
        blur?: number;
        bloomScale?: number;
        threshold?: number;
    }
}

interface OldFilmEffect {
    type: 'oldfilm';
    options?: {
        sepia?: number;
        noise?: number;
    }
}

export type FxFilterEffect = LightningEffect
    | UnderwaterEffect
    | PredatorEffect
    | ColorEffect
    | BloomEffect
    | OldFilmEffect;

export interface FxMasterFilters {
    setFilters(filters: FxFilterEffect[]);
}

export interface FxMaster {
    filters: FxMasterFilters;
}
