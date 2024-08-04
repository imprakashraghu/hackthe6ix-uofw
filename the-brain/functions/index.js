// The Cloud Functions for Firebase SDK to create Cloud Functions and triggers.
const {logger} = require("firebase-functions");
const {onRequest} = require("firebase-functions/v2/https");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");

// The Firebase Admin SDK to access Firestore.
const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const { Storage } = require('@google-cloud/storage');
const uuid = require('uuid')

const serviceAccount = require('./admin.json')

initializeApp();

const storage = new Storage({
  keyFilename: 'admin.json'
})

exports.dataExtraction = onRequest(async (req, res) => {
  
    const categories = [
      { id: 1, name: 'technology', interests: 'apple,nvdia,AI' },
      { id: 2, name: 'top', interests: 'wildfire,olympics,elonmusk' }
    ]
    const countries = [
      // { id: 1, name: 'worldwide', code: 'w' },
      { id: 2, name: 'canada', code: 'ca' },
      { id: 3, name: 'usa', code: 'us' }
    ]

    const languages = [
      { id: 1, name: 'english', code: 'en' },
      { id: 2, name: 'french', code: 'fr' }
    ]

    const resultObj = {}

    for (let country of countries) {
      for (let category of categories) {
        
        // 1. DATA EXTRACTION
        const newsData = await fetchDataFromNewsSourcer(category.name, country.code, 'en', category.interests)
        const news_data = []
        const dumpedDate = formatDateWithPadding(new Date())
        newsData.map((news) => {
          if (news?.description && news_data?.filter(nd => nd?.description === news?.description)?.length === 0) {
            let temp = { title: news.title, description: formatDescription(news?.description||''), link: news.link, pub_date: news.pubDate, image: news.image_url||null, dumped_at: dumpedDate }
            news_data.push(temp)
          }
        })
        resultObj[category.name+'-'+country.name] = news_data
        await getFirestore().collection("news-dumps").doc(dumpedDate).collection(country.name).doc(category.name).set({ articles: news_data })

      }
    }
    
  // Send back a message that we've successfully written the message
  res.json({ result: `Data Extracted!`, data: resultObj });
});

exports.scriptGeneration = onRequest(async (req, res) => {

    const data = await req.body.data;
    const dumpedDate = formatDateWithPadding(new Date())

    const categories = [
      { id: 1, name: 'technology', interests: 'apple,nvdia,AI,tesla' },
      { id: 2, name: 'top', interests: 'wildfire,olympics,elonmusk' }
    ]
    const countries = [
      // { id: 1, name: 'worldwide', code: 'w' },
      { id: 2, name: 'canada', code: 'ca' },
      { id: 3, name: 'usa', code: 'us' }
    ]

    const languages = [
      { id: 1, name: 'english', code: 'en' },
      { id: 2, name: 'french', code: 'fr' }
    ]

    const resultObj = {}

    // 2. PODCAST SCRIPT CREATION
    let tempContentGeneratorHolder = null
    let tempTitleGeneratorHolder = null
    const langHolders = []

    for (let country of countries) {

      for (let category of categories) {

        for (let language of languages) {

          const news_data = data[category.name+'-'+country.name]

          if (language.code === 'en') {
            // script creation
            const queryGeneration = buildQuery(news_data, language.name)

            const completionConfig = {
              model: 'gpt-3.5-turbo-instruct',
              prompt: queryGeneration,
              max_tokens: 3000,
              temperature: 0.8,
              stream: false
            }

            const response = await summarizeFromOpenAI(completionConfig)

            const completionConfigForTitle = {
              model: 'gpt-3.5-turbo-instruct',
              prompt: `Using the following content, write a podcast style title in ${language.name} for the content:\n${response}\nRules: Make it a maximum of 8 words.\n`,
              max_tokens: 1000,
              temperature: 0.8,
              stream: false,
            }

            const titleForContent = await summarizeFromOpenAI(completionConfigForTitle)

            if (response) {

              // saving english podcast to the firestore
              const podcastPayload = {
                title: titleForContent?.replace(/"/g,''),
                audio: null,
                thumbnail: news_data[0]?.image || '',
                content: response+getSuffixAdContent(language.code),
                language: language.name
              }

              resultObj[country.name+'-'+category.name+'-'+language.name] = podcastPayload

              tempTitleGeneratorHolder = titleForContent
              tempContentGeneratorHolder = response+getSuffixAdContent(language.code)

              langHolders.push(podcastPayload)

              await getFirestore().collection('podcasts-new').doc(dumpedDate).collection(country.name).doc(category.name+"-english").set(podcastPayload)

            } else {
              console.log('SCRIPT CREATION FAILED!!!')
            }

          } else {
            // translation service
            const contentFromTranslator = await translatorService(tempContentGeneratorHolder, language.name)
            const titleFromTranslator = await translatorService(tempTitleGeneratorHolder, language.name)

            // signedUrls[0] contains the file's public URL
            const payloadForPodcastTranslated = {
              title: titleFromTranslator,
              content: contentFromTranslator+getSuffixAdContent(language.code),
              audio: null,
              thumbnail: news_data[0]?.image || '',
            }

            resultObj[country.name+'-'+category.name+'-'+language.name] = payloadForPodcastTranslated

            langHolders.push(payloadForPodcastTranslated)
            await getFirestore().collection('podcasts-new').doc(dumpedDate).collection(country.name).doc(category.name+"-french").set(payloadForPodcastTranslated)

          }
        
        }

      }

    }

    res.json({ result: 'Script Generated!', data: resultObj })

});

exports.audioGeneration = onRequest(async (req, res) => {

  const data = await req.body.data;
  const dumpedDate = formatDateWithPadding(new Date())

    const categories = [
      { id: 1, name: 'technology', interests: 'apple,nvdia,AI' },
      { id: 2, name: 'top', interests: 'wildfire,olympics,elonmusk,tesla' }
    ]

    const countries = [
      // { id: 1, name: 'worldwide', code: 'w' },
      { id: 2, name: 'canada', code: 'ca' },
      { id: 3, name: 'usa', code: 'us' }
    ]

    const languages = [
      { id: 1, name: 'english', code: 'en' },
      { id: 2, name: 'french', code: 'fr' }
    ]

    for (let country of countries) {

      for (let category of categories) {

        for (let language of languages) {

          // 3. AUDIO GENERATION
          const audioResponse = await elevenLabsAudioConversion(data[country.name+'-'+category.name+'-'+language.name].content, process.env.VOICE_ID, process.env.ELEVEN_LABS_API_KEY, language.name?.toLowerCase())
          const audioBlob = Buffer.from(audioResponse)
          const fileName = 'podcast-audios/'+dumpedDate+'/'+country.name+'/'+category.name+'/'+language.name+'.mp3'
          const storageRef = storage.bucket('gs://podcast-hm.appspot.com')
          const file = storageRef.file(fileName)

          const token =  uuid.v4();
          const metadata = {
              metadata: {
                firebaseStorageDownloadTokens: token,
              },
              contentType: 'audio/mp3',
              cacheControl: 'public, max-age=31536000',
          };

          file.save(audioBlob, {
            metadata: metadata
          })

          const urls = await file.getSignedUrl({
            action: 'read',
            expires: '12-12-2024'
          })

          getFirestore().collection('podcasts-new').doc(dumpedDate).collection(country.name).doc(category.name+'-'+language.name).update({ audio: urls[0] })

        }

      }
    }

  res.json({ result: 'Podcast Created!' })

});

/**
 * Method used to fetch data (articles) from regulated news sourcer provider - newsdata.io
 * @param category string
 * @param country string
 * @param languageId string
 * @param interests string separated by comma
 */
async function fetchDataFromNewsSourcer(category, country, languageId, interests) {
  const API_KEY = process.env.NEWS_API;
  const countryFilter = country === 'w' ? 'us,ca,in,gb' : country;
  const endpoint = `https://newsdata.io/api/1/news?apiKey=${API_KEY}&category=${category}&country=${countryFilter}&language=${languageId}&q=${interests.replace(/,/g,' OR ')}&image=1&size=8`;
  const response = await fetch(endpoint).then(res => res.json())
  const result = response.results || []
  // console.log('extraction:', result)
  return result;
}

/**
 * Method used to format the description with fillers
 * @param data string
 * @returns 
 */
function formatDescription(data) {
  return data?.replace('[…]',',')
}

/**
 * Method used to format the date as required way
 * @param date string
 * @returns 
 */
function formatDateWithPadding(date) {
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0'); // Month is zero-based, so we add 1
  const year = date.getFullYear();

  return `${day}-${month}-${year}`;
}

/**
 * Method used to create the podcast script using llm (openai - text davincci 003)
 * @param query string
 * @returns 
 */
async function summarizeFromOpenAI(query) {
  // const mockresponse = await fetch('https://api.jsonbin.io/v3/b/64f886ed8d92e126ae67dff6').then(res => res.json())
  // return mockresponse.record.content || null
  const result = await fetch('https://api.openai.com/v1/completions', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${process.env.OPENAI_API_KEY}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(query),
  }).then(res => res.json())
  .catch(err => {
    console.log(err);
    return null 
  })

  // console.log(result);
  
  return result.choices[0].text.replace(/\n/g,'')
}

/**
 * Method used to build the query for the script creation llm
 * @param data string
 * @param language string
 * @returns 
 */
function buildQuery(data, language) {
  let result = `You are the speaker of a podcast titled, "Podcast HM, Your Favorite Updates" sponsored by Hackthe6ix. The purpose of the podcast is to give important updates on what's happened based on the following articles:\n`
  data.forEach((item) => {
    let temp = `Title: ${item.title}\nDescription: ${item.description}\n`
    result += temp
  })
  result += `Write a podcast audio script by summarising the articles into a single paragraph and keep most of the words from the original content. The script should atleast 2000 characters. Make sure the script is with seamless narrative transistion. Content should be in ${language}. Make it one single paragraph without mentionining who speaks it.`;
  return result
}

/**
 * Method used to build the prompt for llm
 * @param content string
 * @param lang string
 * @returns 
 */
function buildQueryForTranslator(content, lang) {
  return `Translate the following text to ${lang}. Make sure to preserve the meaning of the text.\nText:\n${content}`;
}

/**
 * Method used to create the service to translate the podcast content to the target language
 * @param content string
 * @param language string
 * @returns 
 */
async function translatorService(content, language) {
  const queryGeneration = buildQueryForTranslator(content, language)

  const completionConfig = {
    model: 'gpt-3.5-turbo-instruct',
    prompt: queryGeneration,
    max_tokens: 2000,
    temperature: 0.8,
    stream: false,
  }

  const response = await summarizeFromOpenAI(completionConfig)
  
  return response;
}

/**
 * Method used to add suffix content to all the podcast at the end of the script
 * @param language string
 * @returns 
 */
function getSuffixAdContent(language) {
    if (language === 'en') {
      return ` Hey! One last thing, We know that HACK the 6ix at University of Toronto is been the talk over the entire city! just being calm.`;
    } else {
      return ` Hé! Une dernière chose, nous savons que HACK the 6ix à l'Université de Toronto fait parler de lui dans toute la ville ! juste être calme.`
    }
}

function formatDateWithPadding(date) {
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0'); // Month is zero-based, so we add 1
  const year = date.getFullYear();

  return `${day}-${month}-${year}`;
}

/**
 * Method used to create job to convert the text for podcast into audio using ai powered api
 * @param summaryData string
 * @param voiceId string
 * @param apiKey string
 * @param language string
 * @returns 
 */
async function elevenLabsAudioConversion(summaryData, voiceId, apiKey, language) {  
  return await fetch(`https://api.elevenlabs.io/v1/text-to-speech/${voiceId}`,{
    method: 'POST',
    headers: {
        'accept': 'audio/mpeg', // Set the expected response type to audio/mpeg.
        'Content-Type': 'application/json', // Set the content type to application/json.
        'xi-api-key': apiKey, // Set the API key in the headers.
    },
    body: JSON.stringify({text: summaryData, model_id: 'eleven_multilingual_v1'}),
  }).then(res => res.arrayBuffer())
  .catch(err => console.log(err))
}

function formatDateWithPadding(date) {
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0'); // Month is zero-based, so we add 1
  const year = date.getFullYear();

  return `${day}-${month}-${year}`;
}

