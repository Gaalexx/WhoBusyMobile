const apiTarget = process.env.WHOBUSY_API_PROXY_TARGET || "https://whobusy.ru";
const parsedApiTarget = new URL(apiTarget);

if (parsedApiTarget.protocol !== "https:" || parsedApiTarget.username || parsedApiTarget.password ||
    parsedApiTarget.pathname !== "/" || parsedApiTarget.search || parsedApiTarget.hash) {
    throw new Error("WHOBUSY_API_PROXY_TARGET must be an HTTPS origin without credentials, path or query");
}

config.devServer = config.devServer || {};
config.devServer.proxy = [
    {
        context: ["/api"],
        target: parsedApiTarget.origin,
        changeOrigin: true,
        secure: true,
    },
];
