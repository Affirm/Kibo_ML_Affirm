<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<jsp:include page="/admin/include/HeaderInclude.jsp" />

<body>
    <html:form action="deplabstrigger" styleId="oForm">
        <h2>DEPLabs JOB TRIGGER</h2>
        ${deplabsTriggerForm.result}    
    </html:form>

    </br></br></br>
    <h2>HOW TO USE Quartz Job Trigger:</h2>

    <table>
        <tr><td>Log into MLAdmin</td></tr>
        <tr><td colspan=2>Send the following URIs for different operations</td></tr>
        <tr><td>LIST</td><td>/admin/deplabstrigger.do?action=list</td></tr>
        <tr><td>RUN</td><td>/admin/deplabstrigger.do?action=submit&job=JOB_NAME&delay=SECONDS</td></tr>
    </table>


</body>
</html>
