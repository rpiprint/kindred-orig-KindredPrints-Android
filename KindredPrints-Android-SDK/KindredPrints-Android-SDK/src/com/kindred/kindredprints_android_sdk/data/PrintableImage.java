package com.kindred.kindredprints_android_sdk.data;

public class PrintableImage {
	public static final String NO_SERVER_INIT = "no_server_init";
	
	private String serverLineItemId;
	private boolean serverLineItemInit;
	private String serverId;
	private boolean serverInit;
	private PartnerImage image;
	private PrintProduct printType;
	
	public PrintableImage() {
		this.serverId = NO_SERVER_INIT;
		this.serverInit = false;
		this.serverLineItemId = NO_SERVER_INIT;
		this.serverLineItemInit = false;
		this.image = new PartnerImage();
		this.printType = new PrintProduct();
	}
	
	public PrintableImage copy() {
		PrintableImage pCopy = new PrintableImage();
		pCopy.setImage(this.image.copy());
		pCopy.setPrintType(this.printType.copy());
		pCopy.setServerLineItemId(this.getServerLineItemId());
		pCopy.setServerLineItemInit(this.isServerLineItemInit());
		pCopy.setServerId(this.getServerId());
		pCopy.setServerInit(this.isServerInit());
		return pCopy;
	}
	
	public String getServerLineItemId() {
		return serverLineItemId;
	}

	public void setServerLineItemId(String serverLineItemId) {
		this.serverLineItemId = serverLineItemId;
	}

	public boolean isServerLineItemInit() {
		return serverLineItemInit;
	}

	public void setServerLineItemInit(boolean serverLineItemInit) {
		this.serverLineItemInit = serverLineItemInit;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public boolean isServerInit() {
		return serverInit;
	}

	public void setServerInit(boolean serverInit) {
		this.serverInit = serverInit;
	}

	public PartnerImage getImage() {
		return image;
	}

	public void setImage(PartnerImage image) {
		this.image = image;
	}

	public PrintProduct getPrintType() {
		return printType;
	}

	public void setPrintType(PrintProduct printType) {
		this.printType = printType;
	}
	
	
	
}
