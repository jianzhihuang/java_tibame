import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;

@WebServlet("/uploadServlet3.do")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 5 * 5 * 1024 * 1024)
// 當數據量大於fileSizeThreshold值時，內容將被寫入磁碟
// 上傳過程中無論是單個文件超過maxFileSize值，或者上傳的總量大於maxRequestSize 值都會拋出
// IllegalStateException 異常
public class UploadTest_Servlet3 extends HttpServlet {

	String saveDirectory = "/images_uploaded"; // 上傳檔案的目地目錄;
												// 將由底下的第27行用 java.io.File 於 ContextPath 之下, 自動建立目地目錄
	List<File> fileSet;
	int con = 1;
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		req.setCharacterEncoding("UTF-8"); // 處理中文檔名
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		HttpSession session = req.getSession();
		// 創造一個fileSet物件存放file
		if (session.getAttribute("pic_value") != null) {
			fileSet = (LinkedList<File>) session.getAttribute("pic_value");
		} else {
			fileSet = new LinkedList<File>();
			session.setAttribute("pic_value", fileSet);
		}
		// 假如收到參數del跟檔案大於0進行刪除
		if (req.getParameter("del") != null ) {
			System.out.println("del");
			delectfile(req);
		}
		
		
		System.out.println(getServletContext().getRealPath(saveDirectory)); // 測試用
		File fsaveDirectory = new File(getServletContext().getRealPath(saveDirectory));
		if (!fsaveDirectory.exists())
			fsaveDirectory.mkdirs(); // 於 ContextPath 之下,自動建立目地目錄


		// 假如檔案不是空值寫入硬碟
		if (req.getParts() != null) {
			writefile(req, fsaveDirectory);
		}

		// 印出html
		out.println("<HTML><HEAD>");
		out.println("<TITLE>Upload.html</TITLE></HEAD>");
		out.println("<BODY>");
		out.println("<form action=\"\" enctype=\"multipart/form-data\" method=\"POST\">");
		out.println("<p>File: " + fsaveDirectory + "</p>");
		Collection<File> fileCollection = fileSet;
		for (File file : fileCollection) {
			if (file.exists()) {
				out.println("<img src=\"" + req.getContextPath() + saveDirectory + "/" + file.getName() + "\">");
				out.println("<button name=\"del\"  value =\"" + file.getName() + "\">" + file.getName() +"</button>");
				out.println("<hr>");
				out.println("<p>"+con++);
			}
		}
		out.println("<p>file size"+fileSet.size());
		out.println("</form>");
		out.print("</BODY>");
		out.println("</HTML>");
		
	}

	// 取出上傳的檔案名稱 (因為API未提供method,所以必須自行撰寫)
	public String getFileNameFromPart(Part part) {
		String header = part.getHeader("content-disposition");
		System.out.println("header=" + header); // 測試用
		String filename = new File(header.substring(header.lastIndexOf("=") + 2, header.length() - 1)).getName();
		System.out.println("filename=" + filename); // 測試用
		if (filename.length() == 0) {
			return null;
		}
		return filename;

	}

	// 寫檔案
	public void writefile(HttpServletRequest req, File fsaveDirectory) throws IOException, ServletException {

		Collection<Part> parts = req.getParts(); // Servlet3.0新增了Part介面，讓我們方便的進行檔案上傳處理
//		out.write("<h2> Total parts : " + parts.size() + "</h2>");

		for (Part part : parts) {
			if (getFileNameFromPart(part) != null && part.getContentType() != null) {
				if (!part.getName().equals("del")) {
				String filename = getFileNameFromPart(part);
				File f = new File(fsaveDirectory, filename);
				// 利用File物件,寫入目地目錄,上傳成功
				System.out.println("writefile");
				System.out.println(f);
				part.write(f.toString());
				fileSet.add(f);
//				fileSet.add(0, f);

			}
		}
	}
	}
	// 刪除檔案
	public void delectfile(HttpServletRequest req) {
		System.out.println("delfile enter");
		String[] del = req.getParameterValues("del");
		System.out.println(del.length);
		if (del.length > 0) {
			for (int i = 0; i < del.length; i++) {
//				int j = Integer.parseInt(del[i]);
				fileSet.remove(del[i]);
//				System.out.println(j);
			}
			
		}
	}

}
