if (document.readyState != 'loading') {
  bootstrap();
} else {
  document.addEventListener('DOMContentLoaded', bootstrap);
}

function bootstrap() {
  let pages = [];

  fetch('/pages')
    .then((response) => response.json())
    .then((data) => data.forEach((d) => pages.push(d)))
    .then(() => fetch('/policies'))
    .then((response) => response.json())
    .then((data) => {
      const insertPoint = document.querySelector('#pages');
      data.unshift('nocsp'); // CSP free display
      data.forEach((d) => {
        const div = document.createElement('div');
        div.className = 'policy';
        const p = document.createElement('p');
        p.innerText = d;
        div.appendChild(p);
        const ol = document.createElement('ol');

        pages.forEach((pg) => {
          const li = document.createElement('li');
          const a = document.createElement('a');
          a.textContent = pg + ' (' + d + ')';
          a.href = '/' + pg + '?csp=' + d;
          li.appendChild(a);
          ol.appendChild(li);
        });
        div.appendChild(ol);
        insertPoint.appendChild(div);
      });
    })
    .catch(console.error);
}
