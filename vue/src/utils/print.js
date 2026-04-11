/*
 * Copyright (C) 2018-2026 Modding Craft ZBD Studio.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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