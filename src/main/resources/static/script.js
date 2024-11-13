// src/main/resources/static/script.js

document.getElementById('checkBtn').addEventListener('click', () => {
    const statement = document.getElementById('statement').value.trim();
    const checkBtn = document.getElementById('checkBtn');

    if (!statement) {
        displayResult('Please enter a statement.', 'error', []);
        return;
    }

    // Disable the button to prevent multiple clicks
    checkBtn.disabled = true;
    checkBtn.textContent = 'Checking...';

    displayResult('Checking...', 'loading', []);

    fetch('/api/check', {
        method: 'POST',
        headers: {
            'Content-Type': 'text/plain'
        },
        body: statement
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server error: ${response.statusText}`);
            }
            return response.json(); // Changed from response.text() to response.json()
        })
        .then(data => {
            console.log('API Response:', data); // For debugging

            const normalizedResult = data.result.toLowerCase().trim();
            const articles = data.articles || [];

            if (normalizedResult.startsWith('not fake news')) {
                displayResult(data.result, 'not-fake-news', articles);
            } else if (normalizedResult.startsWith('fake news')) {
                displayResult(data.result, 'fake-news', articles);
            } else {
                displayResult(data.result, 'error', articles);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            displayResult('An error occurred while checking the fact.', 'error', []);
        })
        .finally(() => {
            // Re-enable the button
            checkBtn.disabled = false;
            checkBtn.textContent = 'Check Fact';
        });
});

/**
 * Displays the result with appropriate styling and supporting articles.
 * @param {string} message - The message to display.
 * @param {string} type - The type of message ('fake-news', 'not-fake-news', 'error', 'loading').
 * @param {Array} articles - An array of supporting articles.
 */
function displayResult(message, type, articles) {
    const resultDiv = document.getElementById('result');
    const resultText = document.getElementById('result-text');
    const spinner = document.getElementById('spinner');

    // Clear previous articles if any
    const existingArticles = resultDiv.querySelector('.articles');
    if (existingArticles) {
        resultDiv.removeChild(existingArticles);
    }

    if (type === 'loading') {
        spinner.classList.remove('hidden');
        resultText.classList.add('hidden');
    } else {
        spinner.classList.add('hidden');
        resultText.classList.remove('hidden');
        resultText.textContent = message;
    }

    resultDiv.className = `result ${type}`;
    resultDiv.classList.remove('hidden');

    // Display supporting articles if available
    if (type !== 'loading' && articles.length > 0) {
        const articlesContainer = document.createElement('div');
        articlesContainer.classList.add('articles');

        const articlesTitle = document.createElement('h3');
        articlesTitle.textContent = 'Supporting Articles:';
        articlesContainer.appendChild(articlesTitle);

        const articlesList = document.createElement('ul');
        articles.forEach(article => {
            const listItem = document.createElement('li');

            const titleLink = document.createElement('a');
            titleLink.href = article.url;
            titleLink.target = '_blank';
            titleLink.rel = 'noopener noreferrer';
            titleLink.textContent = article.title;
            listItem.appendChild(titleLink);

            // Display Author and Source
            const authorP = document.createElement('p');
            authorP.textContent = `Author: ${article.author}`;
            authorP.classList.add('article-author');
            listItem.appendChild(authorP);

            const sourceP = document.createElement('p');
            sourceP.textContent = `Source: ${article.source}`;
            sourceP.classList.add('article-source');
            listItem.appendChild(sourceP);

            articlesList.appendChild(listItem);
        });
        articlesContainer.appendChild(articlesList);
        resultDiv.appendChild(articlesContainer);
    }

    // Smooth scroll to the result section
    if (type !== 'loading') {
        resultDiv.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}

// Character Counter Implementation
const textarea = document.getElementById('statement');
const charCount = document.getElementById('charCount');
const maxChars = 500;

textarea.addEventListener('input', () => {
    const currentLength = textarea.value.length;
    charCount.textContent = currentLength;
    if (currentLength > maxChars) {
        charCount.style.color = '#ff4d4d';
    } else {
        charCount.style.color = '#666';
    }
});

// Dark Mode Toggle Implementation
const themeToggle = document.getElementById('themeToggle');

themeToggle.addEventListener('click', () => {
    document.body.classList.toggle('dark-mode');
    if (document.body.classList.contains('dark-mode')) {
        themeToggle.textContent = '☀️';
    } else {
        themeToggle.textContent = '🌙';
    }
});
