package ports.soc.ides.controller.event;

import ports.soc.ides.controller.helper.IdesPage;

public class PageChangeEvent extends IdesEvent {

	protected IdesPage requestedPage;
	protected IdesPage pageBefore;
	protected IdesPage pageAfter;

	public IdesPage getRequestedPage() {
		return requestedPage;
	}

	public IdesPage getPageBefore() {
		return pageBefore;
	}

	public IdesPage getPageAfter() {
		return pageAfter;
	}

	public void setRequestedPage(IdesPage requestedPage) {
		this.requestedPage = requestedPage;
	}

	public void setPageBefore(IdesPage pageBefore) {
		this.pageBefore = pageBefore;
	}

	public void setPageAfter(IdesPage pageAfter) {
		this.pageAfter = pageAfter;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PageChangeEvent [requestedPage=").append(requestedPage).append(", pageBefore=").append(pageBefore).append(", pageAfter=").append(pageAfter)
				.append(", toString()=").append(super.toString()).append("]");
		return sb.toString();
	}

}
