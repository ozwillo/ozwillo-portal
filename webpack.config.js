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

const common = {
    entry: {
        dashboard:      [path.join(PATHS.app, 'jsx/dashboard/dashboard.jsx.js')].concat(commonEntryPointsLoadersAndServers),
        profile:        [path.join(PATHS.app, 'jsx/profile/profile.jsx.js')].concat(commonEntryPointsLoadersAndServers),
        sync_fc_profile:[path.join(PATHS.app, 'jsx/profile/synchronize-fc-profile.jsx.js')].concat(commonEntryPointsLoadersAndServers),
        network:        [path.join(PATHS.app, 'jsx/network/network.jsx.js')].concat(commonEntryPointsLoadersAndServers),
        myapps:         [path.join(PATHS.app, 'jsx/appmanagement/myapps.jsx.js')].concat(commonEntryPointsLoadersAndServers),
        appstore:       [path.join(PATHS.app, 'jsx/store/store.jsx.js')].concat(commonEntryPointsLoadersAndServers),
        notifications:  [path.join(PATHS.app, 'jsx/notifications.jsx.js')].concat(commonEntryPointsLoadersAndServers),
        contact:        [path.join(PATHS.app, 'jsx/contact.jsx.js')].concat(commonEntryPointsLoadersAndServers)
    },
    output: {
        path: PATHS.build,
        filename: "[name].bundle.js",
        chunkFilename: "[id].chunk.js",
        publicPath: '/build/'
    },
    plugins: [
        new webpack.optimize.CommonsChunkPlugin({
            names: ["commons", "manifest"],
            minChunks: 3
        }),
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery"
        }),
        new webpack.ContextReplacementPlugin(/moment[\\\/]locale$/, /^\.\/(bg|ca|en|es|fr|it|tr)$/)
    ],
    // we have no .jsx for now but planned to rename from .jsx.js to .jsx
    resolve: { extensions: [ '.js', '.jsx' ] },
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
                        presets: ['env', 'react', 'stage-0']
                    }
                }
            },
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
            dashboard:      devEntryPointsLoadersAndServers,
            profile:        devEntryPointsLoadersAndServers,
            network:        devEntryPointsLoadersAndServers,
            myapps:         devEntryPointsLoadersAndServers,
            store:          devEntryPointsLoadersAndServers,
            notifications:  devEntryPointsLoadersAndServers,
            contact:        devEntryPointsLoadersAndServers
        },
        plugins: [
            new webpack.HotModuleReplacementPlugin(),
            new DashboardPlugin()
        ],
        module: {
            rules: [
                // CSS
                {
                    test: /\.css$/,
                    use: [{
                        loader: "style-loader", // creates style nodes from JS strings
                        options: { sourceMap: true }
                    }, {
                        loader: "css-loader", // translates CSS into CommonJS
                        options: { url: false }
                    }]
                },
                // SASS
                {
                    test: /\.sass$/,
                    use: [{
                        loader: "sass-loader", // compiles Sass to CSS
                        options: { sourceMap: true }
                    }]
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
            new ExtractTextPlugin('style.css')
        ],
        module: {
            rules: [
                // CSS
                {
                    test: /\.css$/,
                    use: [{
                        loader: "style-loader", // creates style nodes from JS strings
                        options: { sourceMap: false }
                    }, {
                        loader: "css-loader", // translates CSS into CommonJS
                        options: { url: false }
                    }]
                },
                // SASS
                {
                    test: /\.sass$/,
                    use: [{
                        loader: "sass-loader", // compiles Sass to CSS
                        options: { sourceMap: false }
                    }]
                }
            ]
        }
    });
}

