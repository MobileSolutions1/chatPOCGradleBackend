package server

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by marcelosenaga on 10/1/15.
 */
@WebServlet(name = "Hello", value = "/hello")
public class HomeController: HttpServlet() {
    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse?) {
        resp?.getWriter()?.write("Hello, World!")
    }
}