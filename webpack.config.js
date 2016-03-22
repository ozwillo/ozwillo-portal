const path = require('path');
const webpack = require('webpack');
const merge = require('webpack-merge');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CleanPlugin = require('clean-webpack-plugin');
const autoprefixer = require('autoprefixer');

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
        profile:        [path.join(PATHS.app, 'jsx/profile.js')].concat(commonEntryPointsLoadersAndServers),
        network:        [path.join(PATHS.app, 'jsx/network/network.jsx.js')].concat(commonEntryPointsLoadersAndServers),
        myapps:         [path.join(PATHS.app, 'jsx/appmanagement/myapps.jsx.js')].concat(commonEntryPointsLoadersAndServers),
        store:          [path.join(PATHS.app, 'jsx/store/store.jsx.js')].concat(commonEntryPointsLoadersAndServers),
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
    resolve: { extensions: [ '', '.js', '.jsx' ] },
    module: {
        loaders: [
            { test: /\.png$/, loader: "url-loader?limit=10000" },
            /* loaders for Font Awesome */
            { test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: "url-loader?limit=10000&mimetype=application/font-woff" },
            { test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: "file-loader" },
            /* to ensure jQuery is loaded before Bootstrap */
            { test: /bootstrap-sass\/assets\/javascripts\//, loader: 'imports?jQuery=jquery' },
            /* loader for JSX / ES6 */
            { test: /\.jsx?$/, loaders: ['react-hot', 'babel?cacheDirectory,presets[]=react,presets[]=es2015'], include: path.join(PATHS.app, 'jsx')}
        ]
    },
    postcss: [ autoprefixer ],
    debug: true
};

// Default configuration
if(TARGET === 'start' || !TARGET) {
    module.exports = merge(common, {
        devtool: 'eval-source-map',
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
        entry: {
            dashboard:      common.entry.dashboard.concat(devEntryPointsLoadersAndServers),
            profile:        common.entry.profile.concat(devEntryPointsLoadersAndServers),
            network:        common.entry.network.concat(devEntryPointsLoadersAndServers),
            myapps:         common.entry.myapps.concat(devEntryPointsLoadersAndServers),
            store:          common.entry.store.concat(devEntryPointsLoadersAndServers),
            notifications:  common.entry.notifications.concat(devEntryPointsLoadersAndServers),
            contact:        common.entry.contact.concat(devEntryPointsLoadersAndServers)
        },
        plugins: [
            new webpack.HotModuleReplacementPlugin()
        ],
        module: {
            loaders: [
                {test: /\.css$/, loaders: ['style', 'css', 'postcss']},
                /* loaders for Bootstrap */
                {test: /\.scss$/, loaders: ['style', 'css', 'postcss', 'sass']}
            ]
        }
    });
}
if(TARGET === 'build' || TARGET === 'stats') {
    module.exports = merge(common, {
        module: {
            loaders: [
                {test: /\.css$/, loader: ExtractTextPlugin.extract('style', 'css!postcss')},
                /* loaders for Bootstrap */
                {test: /\.scss$/, loader: ExtractTextPlugin.extract('style', 'css!postcss!sass')}
            ]
        },
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
            new ExtractTextPlugin('styles.min.css')
        ]
    });
}

