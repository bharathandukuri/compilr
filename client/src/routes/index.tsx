import { createRoute } from "@tanstack/react-router"
import { rootRoute } from "./__root"
import { HomePage } from "@/components/home/HomePage"

export const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  component: HomePage,
})
