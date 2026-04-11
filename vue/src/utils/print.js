export const generateBalloonTxt = (task, formatDateTime) => {
  return `
======= BALLOONS TICKET =======
Balloon : ${task.balloonId}
Team    : ${task.teamName}
Seat    : ${task.teamLocation}
Problem : ${task.problem}
Color   : ${task.colorName}
First   : ${task.isFirst ? "YES" : "NO"}
Time    : ${formatDateTime(task.time)}
== POWERED BY CUIT XCPC TOOL ==
`;
};

export const printBalloonTxt = (task, formatDateTime) => {
  const content = generateBalloonTxt(task, formatDateTime);
  const iframe = document.createElement("iframe");
  iframe.style.cssText = "position:fixed;left:-9999px;top:-9999px;width:1px;height:1px";
  document.body.appendChild(iframe);

  const doc = iframe.contentDocument || iframe.contentWindow.document;
  doc.write(`
<html>
  <head>
    <meta charset="UTF-8">
    <style>
      * { margin: 0; padding: 0; }
      pre {
        font-family: 'Consolas', 'Monaco', monospace;
        font-size: 14px;
        white-space: pre;
        line-height: 1.4;
        padding: 8px;
      }
    </style>
  </head>
  <body><pre>${content}</pre></body>
</html>
`);
  doc.close();

  iframe.onload = () => {
    setTimeout(() => {
      iframe.contentWindow.focus();
      iframe.contentWindow.print();
    }, 200);
  };
};

export const printByIframe = (blobData) => {
  const url = URL.createObjectURL(new Blob([blobData],{type:'application/pdf'}));
  const iframe = document.createElement('iframe');
  iframe.style.display='none';
  iframe.src=url;
  iframe.onload=()=>setTimeout(()=>{
    iframe.contentWindow.focus();
    iframe.contentWindow.print();
    setTimeout(()=>{
      document.body.removeChild(iframe);
      URL.revokeObjectURL(url);
    },1000);
  },500);
  document.body.appendChild(iframe);
};