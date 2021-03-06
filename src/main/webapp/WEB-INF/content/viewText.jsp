<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ include file="../../header.jsp" %>


				<div id="leftBox"> 
				
					<div id="bcbMainButtons" class="basicCentralPanelBar">
						<%@ include file="buttons.jsp" %>
					</div>
				
					<div id="basicCentralPanel">
					
						<div class="basicCentralPanelBar">
							<img alt="" src="img/sql.png" />
							<div class="basicCentralPanelBarText">View Text File ${fileName}</div>
						</div>
						
						<div class="menuBarMain" style="position:relative">
							<img alt="" onclick="back();" title="Back" class="button dicas" src="img/back.png" />
							<c:if test="${edit == 'yes' }">
								<img alt="" onclick="save();" title="Save" class="button dicas" src="img/save.png" />
							</c:if>
						</div>

						<form method="post" action="saveExecutorText" id="frmSave" enctype="multipart/form-data">
							<input type="hidden" id="fileName" name="fileName" value="${fileName}">
							<input type="hidden" id="fileContent" name="fileContent" >
							<input type="hidden" name="idExecutor" value="${idExecutor}">
						</form>
							
						<div class="menuBarMain" style="display:table;height:500px;margin-top:5px">
							<div style="float:left; width:98%">
								<table style="margin-top:10px;margin-left:10px" >
									<tr>
										<td style="padding:0px;">
											<div class="menuBarMain" style="position: absolute;height:470px;margin-bottom:5px;font-size:11px !important;width:97%;">
												<textarea style="border:0px;height:140px" id="code" name="code">${textContent}</textarea>
											</div>
										</td>
									</tr>	
								</table>
							</div>
						</div>							


					</div>					
					
				</div>
				
				<div id="rightBox"> 
					<%@ include file="commonpanel.jsp" %>
				</div>
				
<script>

	$(document).ready(function() {

		codeMirrorEditor = CodeMirror.fromTextArea(document.getElementById("code"), { 
			mode: "xml", 
			indentWithTabs: true,
			smartIndent: true,
			matchBrackets : true,

			<c:if test="${edit != 'yes' }">
				readOnly: true,
			</c:if>
			
			lineNumbers: true,
			lineWrapping:false
        });
		
	});
	
	function back() {
		window.history.back();
	}

	function save() {
		$("#fileContent").val( codeMirrorEditor.getDoc().getValue()  );
		$("#frmSave").submit();
	}
	

</script>				
				
<%@ include file="../../footer.jsp" %>
				