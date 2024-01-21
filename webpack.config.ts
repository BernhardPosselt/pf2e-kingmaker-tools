import {Configuration} from 'webpack';
import TerserPlugin from 'terser-webpack-plugin';

const config: Configuration = {
    entry: './src/index.ts',
    optimization: {
        minimizer: [new TerserPlugin({
            terserOptions: {
                keep_classnames: true,
            },
        })],
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            },
        ],
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js'],
    },
    devtool: 'source-map',
};

// noinspection JSUnusedGlobalSymbols
export default config;
