***REMOVED***
/*
 * © Copyright IBM Corp. 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

/**
 * Handles the loading of dependencies.
 *
 * @author Benjamin Jakobus
 */
 
/**
 * Autoloader for application and system controllers.
 * 
 * @author Benjamin Jakobus
 */
// Flag used to prevent direct script access
if (!defined('SBT_SDK')) {
	define('SBT_SDK', 'IBM Social Business Toolkit');
}

if (!defined('BASE_PATH')) {
	define('BASE_PATH', dirname(__FILE__) );
}
//relative url path used for proxy, services and relative things

$projectDir = explode("/", $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI']);
$protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off' || $_SERVER['SERVER_PORT'] == 443) ? "https://" : "http://";

// Determine base location
$indexDir = $protocol . "$_SERVER[HTTP_HOST]/";

// Remove arguments
if (strpos($indexDir, '?') !== FALSE) {
	$indexDir = substr($indexDir, 0, strpos($indexDir, '?'));
}

for ($i = 1; $i < sizeof($projectDir); $i++) {
	$dir = $projectDir[$i];
	// Remove arguments
	if (strpos($dir, '?') === FALSE) {
		$indexDir = $indexDir . $dir . '/';
	}

	if (defined('IBM_SBT_MOODLE_BLOCK')) {
		$index = $indexDir . 'blocks/ibmsbtk/core/ibm-sbt-sdk-root-id.php';
	} else if (defined('IBM_SBT_WORDPRESS_PLUGIN')) {
			$indexDir = $indexDir . 'wp-content/plugins/sbtk-wp/ibm-sbt-sdk-root-id.php';
	} else {
		$index = $indexDir . 'ibm-sbt-sdk-root-id.php';
	}
	
	$ch = @curl_init($index);
	
	@curl_setopt($ch, CURLOPT_NOBODY, true);
	@curl_exec($ch);
	$retcode = @curl_getinfo($ch, CURLINFO_HTTP_CODE);
	@curl_close($ch);

	if ($retcode == 200) {
		if (defined('IBM_SBT_MOODLE_BLOCK')) {
			$indexDir = $indexDir . 'blocks/ibmsbtk/core/';
		} else if (defined('IBM_SBT_WORDPRESS_PLUGIN')) {
			$indexDir = $indexDir . 'wp-content/plugins/sbtk-wp/';
		}
		break;
	}	
}

// Remove ibm-sbt-sdk-root-id.php from base location
$indexDir = str_replace('ibm-sbt-sdk-root-id.php', '', $indexDir);

// Define the base location
// define('BASE_LOCATION',  $indexDir);
define('BASE_LOCATION',  'https://localhost/wordpress/wp-content/plugins/sbtk-wp/'); // TODO: Remove

spl_autoload_register(function ($class) {
	$coreFile = BASE_PATH . '/system/core/' . $class . '.php';
	$applicationFile = BASE_PATH . '/controllers/' . $class . '.php';
	$applicationEndpointFile = BASE_PATH . '/controllers/endpoint/' . $class . '.php';
	$applicationServicesFile = BASE_PATH . '/controllers/services/' . $class . '.php';
	$model = BASE_PATH . '/models/' . $class . '.php';
	if (file_exists($coreFile)) {
		@include $coreFile;
	} else if (file_exists($applicationFile)) {
		@include $applicationFile;
	} else if (file_exists($model)) {
		@include $model;
	} else if (file_exists($applicationEndpointFile)) {
		@include $applicationEndpointFile;
	} else if (file_exists($applicationServicesFile)) {
		@include $applicationServicesFile;
	}
	
});