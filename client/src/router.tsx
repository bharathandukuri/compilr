import { createRouter } from "@tanstack/react-router"
import { rootRoute } from "./routes/__root"
import { indexRoute } from "./routes/index"
import { compilerRoute } from "./routes/compiler"

const routeTree = rootRoute.addChildren([indexRoute, compilerRoute])

export const router = createRouter({
  routeTree,
  defaultPreload: "intent",
})

declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router
  }
}
