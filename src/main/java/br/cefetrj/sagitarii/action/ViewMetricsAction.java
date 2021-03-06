
package br.cefetrj.sagitarii.action;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import br.cefetrj.sagitarii.metrics.IMetricEntity;
import br.cefetrj.sagitarii.metrics.MetricController;
import br.cefetrj.sagitarii.metrics.MetricType;

@Action (value = "viewMetrics", 
	results = { 
		@Result ( location = "viewMetrics.jsp", name = "ok")
	}, interceptorRefs= { @InterceptorRef("seguranca")	 }) 

@ParentPackage("default")
public class ViewMetricsAction extends BasicActionClass {
	private List<IMetricEntity> metrics;
	private MetricType type;
	
	public String execute () {
		metrics = MetricController.getInstance().getEntities();
		return "ok";
	}

	public List<IMetricEntity> getMetrics() {
		return metrics;
	}
	
	public void setType(MetricType type) {
		this.type = type;
	}
	
	public MetricType getType() {
		return type;
	}
	
}
