/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.web;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author lubos
 */
public class ReportBugServlet extends HttpServlet {
    static private Session session;
    static private String mailTo, secretCode, mailFrom;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletOutputStream os = resp.getOutputStream();
        
        resp.setContentType("text/plain");
        
        try {
            String name, subject, desc, code;
            InternetAddress sender;
            name = req.getParameter("name");
            sender = new InternetAddress(req.getParameter("sender"));
            subject = req.getParameter("subject");
            desc = req.getParameter("text");
            code = req.getParameter("code");
            
            if (!secretCode.equals(code))
                throw new Exception("I won't talk to a robot");
            
            if (isEmpty(name) || isEmpty(subject) || isEmpty(desc))
                throw new Exception("All fields must be filled in");
            
            MimeMessage msg = new MimeMessage(session);
            
            msg.setFrom(new InternetAddress(mailFrom));
            msg.setRecipients(Message.RecipientType.TO, mailTo);
            msg.setSubject("[FatRat] "+subject);
            msg.setSentDate(new Date());
            msg.setText(desc);
            msg.setReplyTo(new Address[] {sender});
            msg.addHeader("X-RemoteIP", req.getRemoteAddr());
            
            Transport.send(msg);
            
            String redir = req.getParameter("redirect");
            if (redir != null)
                resp.sendRedirect(redir);
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            os.print(ex.toString());
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        Properties p = new Properties();
        p.put("mail.smtp.host", config.getInitParameter("smtpServer"));
        p.put("mail.from", mailFrom = config.getInitParameter("emailFrom"));
        mailTo = config.getInitParameter("mailTo");
        secretCode = config.getInitParameter("secretCode");
        session = Session.getDefaultInstance(p, null);
    }
    
    static private boolean isEmpty(String str) {
        if (str == null)
            return true;
        return str.trim().isEmpty();
    }
    
}
