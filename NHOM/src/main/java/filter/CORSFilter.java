

package filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter("/*") // Áp dụng cho tất cả URL
public class CORSFilter implements Filter {

	// Danh sách các origin được phép
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://127.0.0.1:3000",
        "http://localhost:3000",
        "http://localhost:8080", // Thêm các origin khác tại đây
        "http://127.0.0.1:5500"
    );
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		 	HttpServletResponse response = (HttpServletResponse)res;
	        HttpServletRequest request = (HttpServletRequest) req;

	 
	        String origin = request.getHeader("Origin");
	        if (origin != null && ALLOWED_ORIGINS.contains(origin)) 
	            response.setHeader("Access-Control-Allow-Origin", origin);
	        else 
	            // Origin mặc định nếu không hợp lệ (hoặc không đặt)
	            response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:3000");
	        
	        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS,DELETE,PUT");
	        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
	        response.setHeader("Access-Control-Allow-Credentials", "true");

	        // Nếu là preflight request thì trả về OK luôn
	        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
	            response.setStatus(HttpServletResponse.SC_OK);
	            return;
	        }

	        chain.doFilter(req, res); // Cho request đi tiếp
		
	}
	
}

