<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<jsp:include page="/admin/include/HeaderInclude.jsp" />

<body>
    <html:form action="deplabslogger" styleId="oForm">
        <h2>CURRENT LOG4J Categories Config</h2>
        ${deplabsLoggerForm.categoriesLevel}    
    </html:form>

    </br></br></br>
    <h2>HOW TO USE log4j changer:</h2>

    <table>
        <tr><td>Log into MLAdmin</td></tr>
        <tr><td colspan=2>Send the following URIs for different operations</td></tr>
        <tr><td>LIST</td><td>/admin/deplabslogger.do?action=list</td></tr>
        <tr><td>REMOVE</td><td>/admin/deplabslogger.do?action=remove&class=CLASS</td></tr>
        <tr><td>UPDATE</td><td>/admin/deplabslogger.do?action=update&class=CLASS&level=LOG-LEVEL</td></tr>        
    </table>

    <table>
    	<tr><td>Parameters</td></tr>
        <tr><td>CLASS</td><td>=</td><td>your class or package. example:com.deplabs.app or com.deplabs.app.SomeClass</td></tr>
        <tr><td>LOG-LEVEL</td><td>=</td><td>ALL</td></tr>
        <tr><td></td><td></td><td>TRACE</td></tr>
        <tr><td></td><td></td><td>DEBUG</td></tr>
        <tr><td></td><td></td><td>INFO</td></tr>
        <tr><td></td><td></td><td>WARN</td></tr>
        <tr><td></td><td></td><td>ERROR</td></tr>
        <tr><td></td><td></td><td>FATAL</td></tr>
        <tr><td></td><td></td><td>(upper or lower case)</td></tr>
    </table>
</body>
</html>
