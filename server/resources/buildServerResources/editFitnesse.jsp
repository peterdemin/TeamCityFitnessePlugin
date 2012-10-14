<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<l:settingsGroup title="Fitnesse options">
    <tr>
        <th>
            <label for="fitnesseHost">Fitnesse host (ended with slash): </label>
        </th>
        <td>
            <props:textProperty name="fitnesseHost" />
            <span class="error" id="error_fitnesseHost"></span>
            <span class="smallNote">http://localhost:8080/</span>
        </td>
    </tr>
    <tr>
        <th>
            <label for="fitnesseTests">Fitnesse test names</label>
        </th>
        <td>
            <props:textProperty name="fitnesseTests" />
            <span class="error" id="error_fitnesseTests"></span>
            <span class="smallNote">FirstTest;SecondTest;ThirdTest</span>
        </td>
    </tr>
</l:settingsGroup>
