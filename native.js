(()=>{const N=window.Capacitor&&Capacitor.Plugins&&Capacitor.Plugins.Net;if(!N)return;
const pad=n=>String(n).padStart(3,'0'),ipf=async()=>{const r=(await N.ip()).ip;if(!r)throw 0;return r.split('.')};
const toast=t=>{let e=document.getElementById('tst');if(!e){e=document.createElement('div');e.id='tst';e.style.cssText='position:fixed;bottom:12px;left:12px;right:12px;background:#35d07f;color:#000;padding:10px;border-radius:6px;z-index:20';document.body.append(e)}e.textContent=t;e.style.display='block';setTimeout(()=>e.style.display='none',4000)};
N.start();
/* save files to Downloads/MultiCam */
window.dl=async(b,n)=>{const S=3145728;for(let o=0;o<b.size;o+=S){const u=new Uint8Array(await b.slice(o,o+S).arrayBuffer());let s='';for(let i=0;i<u.length;i+=32768)s+=String.fromCharCode.apply(null,u.subarray(i,i+32768));await N.save({name:n,data:btoa(s),done:o+S>=b.size})}toast('Saved to Downloads/MultiCam: '+n)};
/* monitor: 10-digit code = last two IP parts + 4-digit PIN, plus QR */
window.relay=async c=>{try{const ip=await ipf();NT='http://127.0.0.1:8787/';c.code=pad(ip[2])+pad(ip[3])+(1000+Math.floor(Math.random()*9000));
 const e=$('cd'+c.i);e.textContent=c.code;const q=qrcode(0,'M');q.addData(c.code);q.make();e.insertAdjacentHTML('afterend','<img src="'+q.createDataURL(5)+'" style="background:#fff;padding:6px;border-radius:6px;display:block;margin-top:6px">');
 await put('mcs'+c.code+'o',await z(JSON.stringify(c.pc.localDescription)));
 const a=await get('mcs'+c.code+'a',6e5);if(a)await c.pc.setRemoteDescription(JSON.parse(await z(a,1)))}catch(x){$('st'+c.i).textContent='Join a Wi-Fi, hotspot or USB network first';$('cd'+c.i).textContent='-'}};
/* phone: type or scan the code */
const J=window.join;window.join=async()=>{const code=$('cd').value.replace(/\D/g,'');if(code.length!=10)return alert('Enter the 10-digit code');
 try{const ip=await ipf();NT='http://'+ip[0]+'.'+ip[1]+'.'+(+code.slice(0,3))+'.'+(+code.slice(3,6))+':8787/'}catch(x){return alert('Join the same Wi-Fi, hotspot or USB network first')}$('cd').value=code;return J()};
const b=document.createElement('button');b.textContent='Scan QR';$('cd').parentNode.append(b);
b.onclick=async()=>{if(!st)try{await startCam()}catch(e){return}const v=$('pv'),c=document.createElement('canvas'),x=c.getContext('2d');$('cs').textContent='Point the camera at the QR code on the monitor';
 const t=setInterval(()=>{if(!v.videoWidth)return;c.width=v.videoWidth/2;c.height=v.videoHeight/2;x.drawImage(v,0,0,c.width,c.height);const r=jsQR(x.getImageData(0,0,c.width,c.height).data,c.width,c.height);if(r&&/^\d{10}$/.test(r.data)){clearInterval(t);$('cd').value=r.data;join()}},300)};
})();
