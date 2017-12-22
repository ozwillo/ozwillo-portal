const path = require('path');
const webpack = require('webpack');
const merge = require('webpack-merge');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CleanPlugin = require('clean-webpack-plugin');
const DashboardPlugin = require('webpack-dashboard/plugin');

const TARGET = process.env.npm_lifecycle_event;
const PATHS = {
    app: path.join(__dirname, 'src/main/resources/public'),
    build: path.join(__dirname, 'src/main/resources/public/build')
};

const commonEntryPointsLoadersAndServers = ['bootstrap-loader', 'font-awesome-webpack'];
const devEntryPointsLoadersAndServers = ['webpack-dev-server/client?http://localhost:3000', 'webpack/hot/only-dev-server'];

const extractCSS = new ExtractTextPlugin({ filename: 'bundle.css' });

const common = {
    entry: {
        index: [path.join(PATHS.app, 'js/main.js'), path.join(PATHS.app, 'css/index.css')].concat(commonEntryPointsLoadersAndServers)
    },
    output: {
        path: PATHS.build,
        filename: "bundle.js",
        publicPath: '/build/'
    },
    plugins: [
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery"
        }),
        new webpack.ContextReplacementPlugin(/moment[\\\/]locale$/, /^\.\/(bg|ca|en|es|fr|it|tr)$/)
    ],
    module: {
        loaders: [
            /* bootstrap-sass-loader */
            { test: /bootstrap-sass\/assets\/javascripts\//, loader: 'imports-loader?$=jquery' },

            /* loaders for urls */
            { test: /\.png$/, loader: "url-loader?limit=10000" }
        ],
        rules: [
            // JS
            { test: require.resolve("jquery"), use: "imports-loader?$=jquery" },
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['react', 'stage-0',
                            ["env", {
                                "targets": {
                                    "browsers": ["last 2 Chrome versions"]
                                }
                            }]]
                    }
                }
            },

            //Ressources
            {
                test: /.(ttf|otf|eot|svg|woff(2)?)(\?[a-z0-9]+)?$/,
                use: [{
                    loader: 'file-loader',
                    options: {
                        name: '[name].[ext]',
                        outputPath: 'fonts/',    // where the fonts will go
                    }
                }]
            }
        ]
    }
};

// Default configuration
if(TARGET === 'start' || !TARGET) {
    module.exports = merge(common, {
        devServer: {
            publicPath: common.output.publicPath,
            contentBase: '/build',
            hot: true,
            inline: true,
            progress: true,
            stats: { colors: true },
            port: 3000,
            proxy : {
                "*": "http://localhost:8080"
            }
        },
        devtool: 'source-map',
        entry: {
            index: devEntryPointsLoadersAndServers
        },
        plugins: [
            new webpack.HotModuleReplacementPlugin(),
            new DashboardPlugin(),
            extractCSS
        ],
        module: {
            rules: [
                {
                    test: /\.css$/,
                    exclude: /node_modules/,
                    use: extractCSS.extract({
                        fallback: 'style-loader',
                        use: [ 'css-loader' ]
                    })
                },
                {
                    test: /\.scss$/,
                    exclude: /node_modules/,
                    use: extractCSS.extract({
                        fallback: 'style-loader',
                        use: [ 'css-loader', 'sass-loader' ]
                    })
                }
            ]
        }
    });
}

if(TARGET === 'build' || TARGET === 'stats') {
    module.exports = merge(common, {
        plugins: [
            new CleanPlugin([PATHS.build]),
            // Setting DefinePlugin affects React library size!
            // DefinePlugin replaces content "as is" so we need some extra quotes
            // for the generated code to make sense
            new webpack.DefinePlugin({
                'process.env.NODE_ENV': '"production"'
            }),
            new webpack.optimize.UglifyJsPlugin({
                compress: {
                    warnings: false
                }
            }),
            extractCSS
        ],
        module: {
            rules: [
                {
                    test: /\.css$/,
                    exclude: /node_modules/,
                    use: extractCSS.extract({
                        fallback: 'style-loader',
                        use: [
                            {
                                loader: "css-loader", // translates CSS into CommonJS
                                options: { minimize: true }
                            }
                        ]
                    })
                },
                {
                    test: /\.scss$/,
                    exclude: /node_modules/,
                    use: extractCSS.extract({
                        fallback: 'style-loader',
                        use: [
                            {
                                loader: "css-loader", // translates CSS into CommonJS
                                options: { minimize: true }
                            },{
                                loader: 'sass-loader'
                            }
                        ]
                    })
                }
            ]
        }
    });
}

