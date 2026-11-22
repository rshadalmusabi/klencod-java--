<!DOCTYPE html>
<html lang="ar" dir="rtl"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>الحاسبة العلمية المتقدمة</title>
<style>
:root{--bg:#10181c;--pn:#18262c;--k:#22343c;--fg:#e8f1ef;--ac:#f2b134;--op:#2c6e7a;--rd:#c0504d}
body.lt{--bg:#eef3f1;--pn:#fff;--k:#dfe9e6;--fg:#14292c;--ac:#b86a00;--op:#9cd3dc;--rd:#f0a8a6}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--fg);font-family:"Segoe UI",Tahoma,Arial,sans-serif;display:flex;justify-content:center}
#app{width:100%;max-width:520px;padding:10px}
.top{display:flex;gap:6px;margin-bottom:6px}.top button{flex:1;padding:8px;font-size:13px}
#scr{background:var(--pn);border-radius:12px;padding:12px;direction:ltr;text-align:right;min-height:110px}
#expr{width:100%;background:none;border:0;color:var(--fg);font-size:22px;text-align:right;outline:0;direction:ltr}
#res{font-size:34px;color:var(--ac);min-height:44px;word-break:break-all}#err{color:#ff7b7b;font-size:13px;min-height:16px}
.g{display:grid;grid-template-columns:repeat(6,1fr);gap:6px;margin-top:8px;direction:ltr}
button{border:0;border-radius:10px;background:var(--k);color:var(--fg);font:inherit;font-size:16px;padding:12px 0;cursor:pointer}
button:active{transform:scale(.95)}button.o{background:var(--op)}button.e{background:var(--ac);color:#111;font-weight:700}button.c{background:var(--rd)}
.top button{background:var(--k)}.top button.on{background:var(--ac);color:#111}
#his{margin-top:10px;background:var(--pn);border-radius:12px;padding:8px;max-height:150px;overflow:auto;direction:ltr;font-size:14px}
#his div{padding:4px;border-bottom:1px solid var(--k);cursor:pointer}
</style></head><body><div id="app">
<div class="top"><button id="deg" class="on">DEG</button><button id="mem">M: 0</button><button id="thm">◐</button><button id="clh">مسح السجل</button></div>
<div id="scr"><input id="expr" inputmode="none" autocomplete="off" placeholder="0"><div id="err"></div><div id="res"></div></div>
<div class="g" id="g"></div><div id="his"></div></div>
<script>
'use strict';
const $=s=>document.querySelector(s);
let deg=true,mem=0,ans=0,hist=JSON.parse(localStorage.getItem('calcH')||'[]');
const fact=n=>{if(n<0||n!==Math.floor(n))throw'عاملي يتطلب عددًا صحيحًا غير سالب';if(n>170)return Infinity;let r=1;for(let i=2;i<=n;i++)r*=i;return r};
const toR=x=>deg?x*Math.PI/180:x,fromR=x=>deg?x*180/Math.PI:x;
const F={sin:x=>Math.sin(toR(x)),cos:x=>Math.cos(toR(x)),tan:x=>Math.tan(toR(x)),asin:x=>fromR(Math.asin(x)),acos:x=>fromR(Math.acos(x)),atan:x=>fromR(Math.atan(x)),
sinh:Math.sinh,cosh:Math.cosh,tanh:Math.tanh,log:Math.log10,ln:Math.log,sqrt:x=>{if(x<0)throw'جذر عدد سالب';return Math.sqrt(x)},cbrt:Math.cbrt,abs:Math.abs,exp:Math.exp,
fact,floor:Math.floor,ceil:Math.ceil,round:Math.round,
npr:(n,r)=>fact(n)/fact(n-r),ncr:(n,r)=>fact(n)/(fact(r)*fact(n-r)),pow:Math.pow,mod:(a,b)=>a%b,
gcd:(a,b)=>{a=Math.abs(a);b=Math.abs(b);while(b)[a,b]=[b,a%b];return a}};
const K={pi:Math.PI,e:Math.E,ans:()=>ans};
function tokenize(s){const t=[],re=/\s*(?:(\d+\.?\d*(?:[eE][+-]?\d+)?|\.\d+)|([a-zA-Zπ√]+)|([-+*/^!%(),×÷]))/y;let m;s=s.replace(/π/g,'pi').replace(/√/g,'sqrt');re.lastIndex=0;
while(re.lastIndex<s.length){if(/^\s*$/.test(s.slice(re.lastIndex)))break;m=re.exec(s);if(!m)throw'رمز غير صالح عند: '+s.slice(re.lastIndex,re.lastIndex+3);t.push(m[1]!==undefined?{n:+m[1]}:m[2]?{id:m[2].toLowerCase()}:{op:m[3]==='×'?'*':m[3]==='÷'?'/':m[3]})}return t}
function evalExpr(s){const t=tokenize(s);let i=0;const pk=()=>t[i],isOp=o=>t[i]&&t[i].op===o;
function expr(){let v=term();while(isOp('+')||isOp('-')){const o=t[i++].op;const r=term();v=o==='+'?v+r:v-r}return v}
function term(){let v=unary();for(;;){if(isOp('*')||isOp('/')){const o=t[i++].op;const r=unary();if(o==='/'&&r===0)throw'القسمة على صفر';v=o==='*'?v*r:v/r}else if(t[i]&&(t[i].n!==undefined||t[i].id||isOp('('))){v*=unary()}else break}return v}
function unary(){if(isOp('-')){i++;return-unary()}if(isOp('+')){i++;return unary()}return power()}
function power(){let b=post();if(isOp('^')){i++;return Math.pow(b,unary())}return b}
function post(){let v=prim();while(isOp('!')||isOp('%')){v=t[i++].op==='!'?fact(v):v/100}return v}
function prim(){const k=t[i++];if(!k)throw'تعبير ناقص';if(k.n!==undefined)return k.n;
if(k.id){if(isOp('(')){i++;const a=[];if(!isOp(')')){do{if(isOp(','))i++;a.push(expr())}while(isOp(','))}if(!isOp(')'))throw'قوس مفقود';i++;const f=F[k.id];if(!f)throw'دالة غير معروفة: '+k.id;return f(...a)}
if(k.id in K){const c=K[k.id];return typeof c==='function'?c():c}throw'رمز غير معروف: '+k.id}
if(k.op==='('){const v=expr();if(!isOp(')'))throw'قوس مفقود';i++;return v}throw'خطأ في الصيغة'}
const r=expr();if(i<t.length)throw'خطأ في الصيغة';return r}
const fmt=v=>{if(!isFinite(v))return v>0?'∞':v<0?'-∞':'NaN';const r=+v.toPrecision(12);return Math.abs(r)>=1e15||(r!==0&&Math.abs(r)<1e-9)?r.toExponential(8):String(r)};
const ex=$('#expr'),res=$('#res'),err=$('#err');
function live(){err.textContent='';try{if(!ex.value.trim()){res.textContent='';return}res.textContent=fmt(evalExpr(ex.value))}catch(e){res.textContent=''}}
function calc(){try{const v=evalExpr(ex.value);ans=v;const o=fmt(v);hist.unshift([ex.value,o]);hist=hist.slice(0,50);localStorage.setItem('calcH',JSON.stringify(hist));res.textContent=o;ex.value=o;rh()}catch(e){err.textContent='⚠ '+(typeof e==='string'?e:'خطأ في الحساب')}}
function ins(s){const a=ex.selectionStart??ex.value.length,b=ex.selectionEnd??a;ex.value=ex.value.slice(0,a)+s+ex.value.slice(b);ex.focus();ex.setSelectionRange(a+s.length,a+s.length);live()}
function bs(){const a=ex.selectionStart,v=ex.value;if(a>0){ex.value=v.slice(0,a-1)+v.slice(ex.selectionEnd);ex.setSelectionRange(a-1,a-1)}live()}
const KEYS=[['sin(','sin'],['cos(','cos'],['tan(','tan'],['log(','log'],['ln(','ln'],['AC','AC','c'],
['asin(','sin⁻¹'],['acos(','cos⁻¹'],['atan(','tan⁻¹'],['sqrt(','√'],['cbrt(','∛'],['⌫','⌫','c'],
['sinh(','sinh'],['^','xʸ'],['exp(','eˣ'],['!','n!'],['npr(','nPr'],['ncr(','nCr'],
['pi','π'],['e','e'],['(','(','o'],[')',')','o'],['%','%','o'],['ans','Ans'],
['7','7'],['8','8'],['9','9'],['/','÷','o'],['MC','MC'],['M+','M+'],
['4','4'],['5','5'],['6','6'],['*','×','o'],['MR','MR'],['M-','M−'],
['1','1'],['2','2'],['3','3'],['-','−','o'],['abs(','|x|'],['gcd(','gcd'],
['0','0'],['.','.'],[',',','],['+','+','o'],['^2','x²'],['=','=','e']];
$('#g').innerHTML=KEYS.map((k,i)=>`<button data-i="${i}" class="${k[2]||''}">${k[1]}</button>`).join('');
$('#g').onclick=e=>{const b=e.target.closest('button');if(!b)return;const k=KEYS[b.dataset.i][0];
if(k==='AC'){ex.value='';live()}else if(k==='⌫')bs();else if(k==='=')calc();
else if(k==='MC'){mem=0;mm()}else if(k==='MR')ins(String(mem));
else if(k==='M+'||k==='M-'){try{const v=evalExpr(ex.value||'0');mem+=k==='M+'?v:-v;mm()}catch(x){err.textContent='⚠ لا توجد قيمة صالحة'}}
else ins(k)};
const mm=()=>$('#mem').textContent='M: '+fmt(mem);
$('#mem').onclick=()=>{mem=0;mm()};
$('#deg').onclick=e=>{deg=!deg;e.target.textContent=deg?'DEG':'RAD';e.target.classList.toggle('on',deg);live()};
$('#thm').onclick=()=>document.body.classList.toggle('lt');
$('#clh').onclick=()=>{hist=[];localStorage.removeItem('calcH');rh()};
function rh(){$('#his').innerHTML=hist.map((h,i)=>`<div data-i="${i}">${h[0]} = <b>${h[1]}</b></div>`).join('')||'<small>لا يوجد سجل</small>'}
$('#his').onclick=e=>{const d=e.target.closest('div[data-i]');if(d){ex.value=hist[d.dataset.i][0];live()}};
ex.addEventListener('input',live);ex.addEventListener('keydown',e=>{if(e.key==='Enter'){e.preventDefault();calc()}});
ex.inputMode='text';rh();mm();ex.focus();
</script></body></html>