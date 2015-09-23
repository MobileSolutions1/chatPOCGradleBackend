import org.wasabi.app.AppServer

/**
 * Created by bruno on 9/19/15.
 */
public object Main {
    @JvmStatic fun main(args: Array<String>) {

        val server = AppServer()

        server.configuration

        server.get("/", { response.send("Hello World Wasabi!") })

        server.get("/hello", {
            var x = hashMapOf(Pair("hello", "world"), Pair("on", "wasabi"))
            response.send(x, "application/json")
        })

        server.get("/get/:id", { response.send(request.routeParams["id"].toString()) })

        server.start()
    }


}